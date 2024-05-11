package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataPackageExample;
import com.br.marketing.entity.XieChengCollidingDataRob;
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
import com.google.common.base.Joiner;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Description 携程撞库数据处理作业实现类
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
            // 获取当天待执行的清洗任务id
            List<XiechengCollidingDataProcessTask> taskList = getXiechengCollidingDataProcessTasks(apicode, nowDate);
            List<Long> newTaskIds = taskList.stream().map(XiechengCollidingDataProcessTask::getId).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(taskList)) {
                return;
            }

            // 创建线程池
            Integer threadPoolSize = marketingCommonConfig.getXieChengCollidingDataProcessThread();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);

            taskList.forEach((XiechengCollidingDataProcessTask task) -> {
                // 修改任务状态：task_status=1(任务执行中)
                updateTaskStatus(task);

                // 执行数据处理
                int actualNumber = doProcess(task, newTaskIds, threadPool);

                // 更新任务状态和实际数据量级：task_status=2(任务执行完成)
                updateTaskStatusAndActualNumber(task, actualNumber);
            });

            // 关闭线程池
            threadPoolShutDown(threadPool);
        });
    }

    private int doProcess(XiechengCollidingDataProcessTask task, List<Long> newTaskIds, ThreadPoolExecutor threadPool) {
        // False数据清洗
        if (task.getTaskType() == 0) {
            // 根据taskId获取package
            List<XieChengCollidingDataPackage> packages = getPackageByTaskId(task);
            if (CollectionUtils.isEmpty(packages)) {
                return 0;
            }

            return cleanFalseData(task, packages, newTaskIds, threadPool);
        }

        // TRUE数据剔除
        if (task.getTaskType() == 1) {
            return deleteTrueData(task, threadPool);
        }

        return 0;
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库数据清洗作业线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.error("携程撞库数据清洗作业，日志保存线程池结束异常！", ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 更新任务状态和实际数据量级
     * @param task
     * @param actualNumber
     */
    private void updateTaskStatusAndActualNumber(XiechengCollidingDataProcessTask task, int actualNumber) {
        task.setActualNumber(actualNumber);
        task.setTaskStatus(2);
        task.setTaskEndTime(new Date());
        task.setUpdateTime(new Date());
        taskMapper.updateByPrimaryKeySelective(task);
    }

    /**
     * 根据taskId获取package
     * @param task
     * @return
     */
    private List<XieChengCollidingDataPackage> getPackageByTaskId(XiechengCollidingDataProcessTask task) {
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andCollidingDataTaskIdEqualTo(task.getId()).andIsDeleteEqualTo(0);
        List<XieChengCollidingDataPackage> packages = packageMapper.selectByExample(packageExample);
        return packages;
    }

    /**
     * 跑分数据清洗
     * @param task
     * @param packages
     * @param newTaskIds
     * @param threadPool
     * @return
     */
    private int cleanFalseData(XiechengCollidingDataProcessTask task, List<XieChengCollidingDataPackage> packages, List<Long> newTaskIds,
                               ThreadPoolExecutor threadPool) {
        int actualNumber;
        XieChengCollidingDataPackage newPackage = packages.get(0);
        Long packageId = newPackage.getId();
        log.warn("携程撞库数据清洗任务,当前任务id：{},当前数据包id：{}", task.getId(), packageId);

        // 从老包删除数据并插入新包
        moveRuleScoreDataFromOldToNew(task, newTaskIds, threadPool, newPackage);

        // 获取新数据包量级
        actualNumber = robMapper.selectCountFromRobByNewPackageId(packageId).intValue();
        return actualNumber;
    }

    /**
     * 从老包删除数据并插入新包
     * @param task
     * @param newTaskIds
     * @param threadPool
     * @param newPackage
     */
    private void moveRuleScoreDataFromOldToNew(XiechengCollidingDataProcessTask task, List<Long> newTaskIds, ThreadPoolExecutor threadPool,
                                               XieChengCollidingDataPackage newPackage) {
        // 找到待删除数据的数据包并删除数据
        findOldPackagesAndDelete(newPackage, task, newTaskIds, threadPool);

        // 将跑分数据插入新数据包
        insertRuleScoreDataToNewPackage(newPackage, task, threadPool);
    }

    private void updateTaskStatus(XiechengCollidingDataProcessTask task) {
        task.setUpdateTime(new Date());
        task.setTaskStatus(1);
        taskMapper.updateByPrimaryKeySelective(task);
    }

    /**
     * 获取待执行的清洗任务
     * is_delete=0 && task_status=0(任务待执行) && task_start_time=当天 && order by create_time
     * @param apicode
     * @param nowDate
     * @return
     */
    private List<XiechengCollidingDataProcessTask> getXiechengCollidingDataProcessTasks(String apicode, Date nowDate) {
        XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
        taskExample.createCriteria().andApiCodeEqualTo(apicode).andIsDeleteEqualTo(0)
                .andTaskStatusEqualTo(0).andTaskStartTimeEqualTo(nowDate);
        taskExample.setOrderByClause("create_time asc");
        List<XiechengCollidingDataProcessTask> taskList = taskMapper.selectByExample(taskExample);
        return taskList;
    }

    /**
     * TRUE数据剔除
     * @param task
     * @param threadPool
     * @return
     */
    private int deleteTrueData(XiechengCollidingDataProcessTask task, ThreadPoolExecutor threadPool) {
        AtomicInteger totalDeleteCount = new AtomicInteger(0);
        Long minId = null;
        String conditions = task.getTaskExecutionConditions();
        String extend = "携程撞库数据清洗任务删除，任务id：" + task.getId();
        for (String batchNumber : task.getBatchNumber().split(",")) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }

            String queryRuleScoreDataSql = "select id, cell, is_delete from b_xiecheng_colliding_"
                    + batchNumber + " where " + conditions;

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            while (true) {
                List<Long> longs = cycleMapper.selectIdsOfTrueDataProcessTasktikv_(minId, queryRuleScoreDataSql);
                if (CollectionUtils.isEmpty(longs)) {
                    break;
                }

                modifyThreadPool(threadPool);
                minId = longs.get(longs.size() - 1);

                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        totalDeleteCount.addAndGet(cycleMapper.updateIsDeleteByIds(longs, extend));
                    } catch (Exception e) {
                        log.error("携程撞库TRUE数据删除，单线程处理异常：" + e.getMessage(), e);
                    }
                }, threadPool));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // 发送钉钉告警
        int count = totalDeleteCount.get();
        if (count > 0) {
            String msg = "携程撞库周期TRUE数据删除量级:" + count;
            Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
            Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.XIECHENG_TRUE_DELETE_NOTICE.toString());

            dingDingRobotHookService.sendDingDingTextMessage(msg, map);
        }

        return count;
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

    /**
     * 找到待删除数据的数据包并删除数据
     * @param newPackage
     * @param task
     * @param newTaskIds
     * @param threadPool
     */
    private void findOldPackagesAndDelete(XieChengCollidingDataPackage newPackage, XiechengCollidingDataProcessTask task, List<Long> newTaskIds,
                                          ThreadPoolExecutor threadPool) {
        // 获取所有有效的旧数据包
        List<XieChengCollidingDataPackage> oldPackages = getOldPackages(newTaskIds);
        if (CollectionUtils.isEmpty(oldPackages)) {
            return;
        }

        List<Long> oldPackageIds = oldPackages.stream().map(XieChengCollidingDataPackage::getId).collect(Collectors.toList());
        log.warn("携程撞库数据清洗任务，旧数据包id:{}", Joiner.on(",").join(oldPackageIds));

        // 找到要保留的数据包：旧包优先级大于等于新包优先级&&清洗时间小于等于旧包最大结束时间
        List<Long> priorityPackageIds =
                oldPackages.stream().filter((XieChengCollidingDataPackage t) -> t.getPriority() <= newPackage.getPriority())
                        .map(XieChengCollidingDataPackage::getId).collect(Collectors.toList());
        log.warn("携程撞库数据清洗任务，旧包优先级大于等于新包优先级id:{}", Joiner.on(",").join(priorityPackageIds));

        List<Long> reserveIds;
        if (CollectionUtils.isEmpty(priorityPackageIds)) {
            reserveIds = new ArrayList<>();
        } else {
            List<XiechengCollidingDataPackageRule> maxEndTimeGroupByPackageId = packageRuleMapper.getMaxEndTimeGroupByPackageId(priorityPackageIds);
            log.warn("携程撞库数据清洗任务，大优先级旧包，最大撞库结束时间:{}", Joiner.on(",").join(maxEndTimeGroupByPackageId));

            // 获取清洗时间小于等于旧包最大结束时间的数据包
            reserveIds = getReserveIds(task, maxEndTimeGroupByPackageId);
        }
        log.warn("携程撞库数据清洗任务，旧包优先级大于等于新包优先级&&清洗时间小于等于旧包最大结束时间，数据包id:{}", Joiner.on(",").join(reserveIds));

        // 获取待删除的数据包id
        List<XieChengCollidingDataPackage> deletePackages =
                oldPackages.stream().filter(t -> !reserveIds.contains(t.getId())).collect(Collectors.toList());
        log.warn("携程撞库数据清洗任务，旧包待剔除数据的数据包:{}", Joiner.on(",").join(deletePackages));

        if (CollectionUtils.isEmpty(deletePackages)) {
            return;
        }

        // 根据数据包删除false表和跑分结果交集数据
        deleteFalseDataByPid(task, threadPool, deletePackages);
    }

    /**
     * 获取清洗时间小于等于旧包最大结束时间的数据包
     * @param task
     * @param maxEndTimeGroupByPackageId
     * @return
     */
    private List<Long> getReserveIds(XiechengCollidingDataProcessTask task, List<XiechengCollidingDataPackageRule> maxEndTimeGroupByPackageId) {
        if (CollectionUtils.isEmpty(maxEndTimeGroupByPackageId)) {
            return new ArrayList<>();
        }

        List<Long> reserveIds;
        // 获取当前任务清洗时间
        LocalDate cleanDate = task.getTaskStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        reserveIds =
                maxEndTimeGroupByPackageId.stream()
                        .filter((XiechengCollidingDataPackageRule t) -> t.getCollidingEndTime() != null)
                        .filter((XiechengCollidingDataPackageRule t) -> {
                            LocalDate collidingMaxDate = t.getCollidingEndTime().toInstant()
                                    .atZone(ZoneId.systemDefault()).toLocalDate();

                            return !cleanDate.isAfter(collidingMaxDate);
                        }).map(XiechengCollidingDataPackageRule::getPackageId).collect(Collectors.toList());
        return reserveIds;
    }

    /**
     * 根据数据包删除false表和跑分结果交集数据
     * @param task
     * @param threadPool
     * @param deletePackages
     */
    private void deleteFalseDataByPid(XiechengCollidingDataProcessTask task, ThreadPoolExecutor threadPool,
                                      List<XieChengCollidingDataPackage> deletePackages) {
        Long minId = null;
        String extend = "携程撞库数据清洗任务删除，任务id：" + task.getId();
        String conditions = task.getTaskExecutionConditions();
        for (String batchNumber : task.getBatchNumber().split(",")) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (XieChengCollidingDataPackage deletePackage : deletePackages) {
                String queryRuleScoreDataSql = "select cell, is_delete from b_xiecheng_colliding_" + batchNumber + " where " + conditions;
                log.warn("携程撞库数据清洗任务，旧包剔除数据查询条件:{}", queryRuleScoreDataSql);
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
                    futures.add(CompletableFuture.runAsync(() -> {
                        try {
                            robMapper.updateDeleteByIds(ids, extend);
                        } catch (Exception e) {
                            log.error("携程撞库FALSE数据删除，单线程处理异常：" + e.getMessage(), e);
                        }
                    }, threadPool));
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
        }
    }

    /**
     * 获取所有有效的旧数据包
     * @param newTaskIds
     * @return
     */
    private List<XieChengCollidingDataPackage> getOldPackages(List<Long> newTaskIds) {
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andIsDeleteEqualTo(0).andCollidingDataTaskIdNotIn(newTaskIds);
        return packageMapper.selectByExample(packageExample);
    }

    /**
     * 将跑分数据插入新数据包
     * @param collidingDataPackage
     * @param task
     * @param threadPool
     */
    private void insertRuleScoreDataToNewPackage(XieChengCollidingDataPackage collidingDataPackage, XiechengCollidingDataProcessTask task,
                                                 ThreadPoolExecutor threadPool) {
        Long minId = null;
        String conditions = task.getTaskExecutionConditions();
        for (String batchNumber : task.getBatchNumber().split(",")) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }

            String queryRuleScoreDataSql = "select id, cell, is_delete from b_xiecheng_colliding_" + batchNumber + " where " + conditions;
            log.warn("携程撞库数据清洗任务，新包新增数据查询条件:{}", queryRuleScoreDataSql);

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            while (true) {
                List<XieChengRuleScoreData> ruleScoreData = ruleScoreRecordMapper.selectRuleScoreDataExcludeTrueAndFalseDatatikv_(minId,
                        queryRuleScoreDataSql);
                if (CollectionUtils.isEmpty(ruleScoreData)) {
                    break;
                }

                minId = ruleScoreData.get(ruleScoreData.size() - 1).getId();

                modifyThreadPool(threadPool);
                futures.add(CompletableFuture.runAsync(() -> insertData(ruleScoreData, collidingDataPackage), threadPool));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    /**
     * 数据插入false表新数据包
     * @param ruleScoreData
     * @param collidingDataPackage
     */
    private void insertData(List<XieChengRuleScoreData> ruleScoreData, XieChengCollidingDataPackage collidingDataPackage) {
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
