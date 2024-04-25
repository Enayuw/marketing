package com.br.marketing.service.Impl.xc;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataPackageExample;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataRobPriority;
import com.br.marketing.entity.XieChengRuleScoreData;
import com.br.marketing.entity.XiechengCollidingDataPackageRule;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleExample;
import com.br.marketing.entity.XiechengCollidingDataProcessTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTaskExample;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleMapper;
import com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description XieChengCollidingDataProcessServiceImpl
 * @Author hong.chen
 * @CreateTime 2024/04/24
 */
@Service
@Slf4j
public class XieChengCollidingDataProcessServiceImpl implements XieChengCollidingDataProcessService {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    XiechengCollidingDataProcessTaskMapper taskMapper;
    @Resource
    private AlarmApiClient alarmClient;
    @Resource
    XieChengCollidingDataLoopCycleMapper cycleMapper;
    @Resource
    XieChengCollidingDataRobMapper robMapper;
    @Resource
    XieChengRuleScoreRecordMapper ruleScoreRecordMapper;
    @Resource
    XieChengCollidingDataPackageMapper packageMapper;
    @Resource
    XiechengCollidingDataPackageRuleMapper packageRuleMapper;


    @Override
    public void process() {
        LocalDate localDate = LocalDate.now();
        Date nowDate = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach(apicode -> {
            XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apicode).andIsDeleteEqualTo(0)
                    .andTaskStatusEqualTo(0).andTaskStartTimeEqualTo(nowDate);
            taskExample.setOrderByClause("create_time asc");
            List<XiechengCollidingDataProcessTask> taskList = taskMapper.selectByExample(taskExample);

            Integer threadPoolSize = marketingCommonConfig.getXieChengCollidingDataProcessThread();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
            taskList.forEach(task -> {
                if (task.getTaskType() == 0) {
                    cleanFalseData(task, threadPool);
                }

                if (task.getTaskStatus() == 1) {
                    deleteTrueData(task, threadPool);
                }
            });

            threadPool.shutdown();
            try {
                while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.info("携程撞库数据处理作业线程池关闭");
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
                log.error("携程撞库数据处理作业，日志保存线程池结束异常！", ex);
                Thread.currentThread().interrupt();
            }
        });


    }

    private void deleteTrueData(XiechengCollidingDataProcessTask task, ThreadPoolExecutor threadPool) {
        Long minId = null;
        String conditions = task.getTaskExecutionConditions();
        for (String batchNumber : task.getBatchNumber().split(",")) {
            String queryRuleScoreDataSql = "select cell from b_xiecheng_colliding_" + batchNumber + " where " + conditions;
            while (true) {
                List<Long> longs = cycleMapper.selectIdsOfTrueDataProcessTask(minId, queryRuleScoreDataSql);
                if (CollectionUtils.isEmpty(longs)) {
                    break;
                }

                modifyThreadPool(threadPool);
                minId = longs.get(longs.size() - 1);
                threadPool.submit(() -> cycleMapper.updateIsDeleteByIds(longs));
            }
        }


        // todo 钉钉告警
    }

    private void cleanFalseData(XiechengCollidingDataProcessTask task, ThreadPoolExecutor threadPool) {
        // todo select cell from
        // 查基底数据
        // 关联rob表 保留没关联上的
        //   1.关联上的判断清洗时间小于撞库最大结束时间，且优先级小于旧包优先级：内存剔除
        //   2.其他：内存保留，根据id数据库删除
        // 插入新包

        // 查询package
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andCollidingDataTaskIdEqualTo(task.getId()).andIsDeleteEqualTo(0);
        List<XieChengCollidingDataPackage> packages = packageMapper.selectByExample(packageExample);
        if (CollectionUtils.isEmpty(packages)) {
            return;
        }
        XieChengCollidingDataPackage collidingDataPackage = packages.get(0);

        Long minId = null;
        String conditions = task.getTaskExecutionConditions();
        for (String batchNumber : task.getBatchNumber().split(",")) {
            String queryRuleScoreDataSql = "select id, cell from b_xiecheng_colliding_" + batchNumber + " where " + conditions;
            while (true) {
                List<XieChengRuleScoreData> scoreDataExcludeTrueData = ruleScoreRecordMapper.selectRuleScoreDataExcludeTrueData(minId,
                        queryRuleScoreDataSql);
                if (CollectionUtils.isEmpty(scoreDataExcludeTrueData)) {
                    break;
                }

                minId = scoreDataExcludeTrueData.get(scoreDataExcludeTrueData.size() - 1).getId();
                Set<String> cells = scoreDataExcludeTrueData.stream().map(XieChengRuleScoreData::getCell).collect(Collectors.toSet());

                modifyThreadPool(threadPool);
                threadPool.submit(() -> deleteAndInsertFalseData(collidingDataPackage, task, new ArrayList<>(cells)));
            }
        }
    }

    private void deleteAndInsertFalseData(XieChengCollidingDataPackage collidingDataPackage, XiechengCollidingDataProcessTask task,
                                          List<String> cells) {
        // 重复数据
        List<XieChengCollidingDataRobPriority> robList = robMapper.selectRobDataByRuleScoreData(cells);

        // 旧包优先级大于等于新包优先级的重复数据
        List<String> robPriorityCells =
                robList.stream().filter(rob -> rob.getPriority() >= collidingDataPackage.getPriority()).collect(Collectors.toList()).stream().map(XieChengCollidingDataRob::getCellSha256CodeList).collect(Collectors.toList());

        List<XieChengCollidingDataRobPriority> robMaxCollidingEndTimeList = robMapper.selectMaxCollidingEndTimeGroupByCell(robPriorityCells);
        List<String> exculeCells = robMaxCollidingEndTimeList.stream().filter(t -> {
            // todo 转年月日小于等于
            if (task.getTaskStartTime().before(t.getCollidingEndTime())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList()).stream().map(XieChengCollidingDataRob::getCellSha256CodeList).collect(Collectors.toList());


        ArrayList<XieChengCollidingDataRobPriority> robListCopy = new ArrayList<>(robList);
        robListCopy.stream().map(XieChengCollidingDataRob::getCellSha256CodeList).collect(Collectors.toList()).removeAll(exculeCells);
        List<String> deleteCells = robListCopy.stream().map(XieChengCollidingDataRob::getCellSha256CodeList).collect(Collectors.toList());

        robMapper.updateBatchByCellToIsDeleted(deleteCells);

        // 关联rob表 保留没关联上的
        // task_start_time小于等于旧包最大结束时间，且旧包优先级大于等于当前包优先级：从当前包中剔除
        cells.removeAll(exculeCells);

        List<XieChengCollidingDataRob> insertRobList = cells.stream().map(cell -> {
            XieChengCollidingDataRob rob = new XieChengCollidingDataRob();
            rob.setPackageId(collidingDataPackage.getId());
            rob.setCellSha256CodeList(cell);
            rob.setCreateTime(new Date());
            rob.setUpdateTime(new Date());
            rob.setDataSourceType("F");
            return rob;
        }).collect(Collectors.toList());

        robMapper.saveBatch(insertRobList);
    }


    /**
     * 修改线程池大小
     * @param pool
     */
    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getXieChengCollidingDataProcessThread();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
    }
}
