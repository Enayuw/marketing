package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataPackageExample;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataRobPriority;
import com.br.marketing.entity.XieChengRuleScoreData;
import com.br.marketing.entity.XiechengCollidingDataPackageRule;
import com.br.marketing.entity.XiechengCollidingDataProcessTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTaskExample;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleMapper;
import com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    @Autowired
    private DingDingRobotHookService dingDingRobotHookService;

    @Override
    public void process() {
        LocalDate localDate = LocalDate.now();
        Date nowDate = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach((String apicode) -> {
            XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apicode).andIsDeleteEqualTo(0)
                    .andTaskStatusEqualTo(0).andTaskStartTimeEqualTo(nowDate);
            taskExample.setOrderByClause("create_time asc");
            List<XiechengCollidingDataProcessTask> taskList = taskMapper.selectByExample(taskExample);

            taskList.forEach((XiechengCollidingDataProcessTask task) -> {
                if (task.getTaskType() == 0) {
                    // 查询package
                    XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
                    packageExample.createCriteria().andCollidingDataTaskIdEqualTo(task.getId()).andIsDeleteEqualTo(0);
                    List<XieChengCollidingDataPackage> packages = packageMapper.selectByExample(packageExample);
                    if (CollectionUtils.isEmpty(packages)) {
                        return;
                    }
                    XieChengCollidingDataPackage newPackage = packages.get(0);

                    deleteFromOldPackage(newPackage, task);

                    insertToNewPackage(newPackage, task);
                }

                if (task.getTaskType() == 1) {
                    deleteTrueData(task);
                }

                // 更新结束时间
                task.setTaskEndTime(new Date());
                task.setUpdateTime(new Date());
                taskMapper.updateByPrimaryKeySelective(task);
            });
        });


    }

    private void deleteTrueData(XiechengCollidingDataProcessTask task) {
        AtomicInteger totalDeleteCount = new AtomicInteger(0);
        Long minId = null;
        String conditions = task.getTaskExecutionConditions();
        for (String batchNumber : task.getBatchNumber().split(",")) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }

            String queryRuleScoreDataSql = "select cell from b_xiecheng_colliding_" + batchNumber + " where " + conditions;

            Integer threadPoolSize = marketingCommonConfig.getXieChengCollidingDataProcessThread();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);

            while (true) {
                List<Long> longs = cycleMapper.selectIdsOfTrueDataProcessTasktikv_(minId, queryRuleScoreDataSql);
                if (CollectionUtils.isEmpty(longs)) {
                    break;
                }

                modifyThreadPool(threadPool);
                minId = longs.get(longs.size() - 1);
                threadPool.submit(() -> {
                    try {
                        totalDeleteCount.addAndGet(cycleMapper.updateIsDeleteByIds(longs));
                    } catch (Exception e) {
                        log.error("携程撞库TRUE数据删除，单线程处理异常：" + e.getMessage(), e);
                    }
                });
            }

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
        }

        // 发送钉钉告警
        if (totalDeleteCount.get() > 0) {
            String msg = "携程撞库周期TRUE数据删除量级:" + totalDeleteCount.get();
            Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
            Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.XIECHENG_TRUE_DELETE_NOTICE.toString());

            dingDingRobotHookService.sendDingDingTextMessage(msg, map);
        }

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

    private void deleteFromOldPackage(XieChengCollidingDataPackage newPackage, XiechengCollidingDataProcessTask task) {
        // 找到所有有效的旧数据包
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andIsDeleteEqualTo(0).andCollidingDataTaskIdNotEqualTo(task.getId());
        List<XieChengCollidingDataPackage> oldPackages = packageMapper.selectByExample(packageExample);

        // 找到要保留的数据包：旧包优先级大于等于新包优先级&&清洗时间小于等于旧包最大结束时间
        List<Long> priorityPackageIds =
                oldPackages.stream().filter((XieChengCollidingDataPackage t) -> t.getPriority() >= newPackage.getPriority())
                        .map(XieChengCollidingDataPackage::getId).collect(Collectors.toList());

        List<XiechengCollidingDataPackageRule> maxEndTimeGroupByPackageId = packageRuleMapper.getMaxEndTimeGroupByPackageId(priorityPackageIds);

        List<Long> reserveIds =
                maxEndTimeGroupByPackageId.stream()
                        .filter((XiechengCollidingDataPackageRule t) -> t.getCollidingEndTime() != null)
                        .filter((XiechengCollidingDataPackageRule t) -> {
            LocalDate cleanDate = task.getTaskStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate collidingMaxDate = t.getCollidingEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (!cleanDate.isAfter(collidingMaxDate)) {
                return true;
            }
            return false;
        }).map(XiechengCollidingDataPackageRule::getPackageId).collect(Collectors.toList());

        // 遍历要剔除的数据包，关联跑分和true表，根据id删除
        List<XieChengCollidingDataPackage> deletePackages =
                oldPackages.stream().filter(t -> !reserveIds.contains(t.getId())).collect(Collectors.toList());

        Integer threadPoolSize = marketingCommonConfig.getXieChengCollidingDataProcessThread();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);

        Long minId = null;
        String conditions = task.getTaskExecutionConditions();
        for (String batchNumber : task.getBatchNumber().split(",")) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }

            for (XieChengCollidingDataPackage deletePackage : deletePackages) {
                String queryRuleScoreDataSql = "select cell from b_xiecheng_colliding_" + batchNumber + " where " + conditions;

                while (true) {
                    List<XieChengCollidingDataRob> repeatWithFalseData = ruleScoreRecordMapper.selectRuleScoreDataRepeatWithFalseDatatikv_(minId,
                            deletePackage.getId(),
                            queryRuleScoreDataSql);
                    if (CollectionUtils.isEmpty(repeatWithFalseData)) {
                        break;
                    }

                    List<Long> ids = repeatWithFalseData.stream().map(XieChengCollidingDataRob::getId).collect(Collectors.toList());
                    minId = repeatWithFalseData.get(repeatWithFalseData.size() - 1).getId();

                    modifyThreadPool(threadPool);
                    threadPool.submit(() -> {
                        try {
                            robMapper.updateDeleteByIds(ids);
                        } catch (Exception e) {
                            log.error("携程撞库FALSE数据删除，单线程处理异常：" + e.getMessage(), e);
                        }
                    });
                }
            }
        }

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
    }

    private void insertToNewPackage(XieChengCollidingDataPackage collidingDataPackage, XiechengCollidingDataProcessTask task) {
        Long minId = null;
        String conditions = task.getTaskExecutionConditions();
        for (String batchNumber : task.getBatchNumber().split(",")) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }

            String queryRuleScoreDataSql = "select id, cell from b_xiecheng_colliding_" + batchNumber + " where " + conditions;

            Integer threadPoolSize = marketingCommonConfig.getXieChengCollidingDataProcessThread();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);

            while (true) {
                List<XieChengRuleScoreData> ruleScoreData = ruleScoreRecordMapper.selectRuleScoreDataExcludeTrueAndFalseDatatikv_(minId,
                        queryRuleScoreDataSql);
                if (CollectionUtils.isEmpty(ruleScoreData)) {
                    break;
                }

                minId = ruleScoreData.get(ruleScoreData.size() - 1).getId();

                modifyThreadPool(threadPool);
                threadPool.submit(() -> insertDataToNewPackage(ruleScoreData, collidingDataPackage));
            }

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
        }
    }

    private void insertDataToNewPackage(List<XieChengRuleScoreData> ruleScoreData, XieChengCollidingDataPackage collidingDataPackage) {
        try {
            List<XieChengCollidingDataRob> insertRobList = ruleScoreData.stream().map((XieChengRuleScoreData t) -> {
                XieChengCollidingDataRob rob = new XieChengCollidingDataRob();
                rob.setPackageId(collidingDataPackage.getId());
                rob.setCellSha256CodeList(t.getCell());
                rob.setCreateTime(new Date());
                rob.setUpdateTime(new Date());
                rob.setDataSourceType("F");
                return rob;
            }).collect(Collectors.toList());

            robMapper.saveBatch(insertRobList);
        } catch (Exception e) {
            log.error("携程撞库FALSE数据插入，单线程处理异常：" + e.getMessage(), e);
        }
    }

}
