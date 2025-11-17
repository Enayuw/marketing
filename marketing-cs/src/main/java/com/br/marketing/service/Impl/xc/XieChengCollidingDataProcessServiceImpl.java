package com.br.marketing.service.Impl.xc;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.enums.XcProcessTaskEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.vo.XiechengCollidingTaskBatchVo;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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
    @Resource
    XiechengCollidingTaskBatchMapper taskBatchMapper;

    @Autowired
    RedisChgService redisChgService;

    private final static int PAGE_SIZE = 10000;

    private final static int PARTATION_SIZE = 2000;

    private final static String EXTEND_DELETE_PREFIX = "携程撞库数据清洗任务删除，任务id：";

    private final static String EXTEND_DYNA_DELETE_PREFIX = "携程撞库数据清洗任务动态包删除，任务id：";

    private final static String ERROR_PREFIX_DELETE = "携程撞库true数据剔除，单线程处理异常：，batchId=";

    private final static String ERROR_PREFIX_DYNA_DELETE = "携程撞库false动态包数据剔除，单线程处理异常：，batchId=";

    @Override
    public void process() {
        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach((String apiCode) -> {
            //1.创建线程池
            ThreadPoolExecutor threadPool = getThreadPoolExecutor();
            //2.剔除流程
            deleteProcess(apiCode, XcProcessTaskEnum.PROCESS_DELETE, threadPool);
            //3.当天所有true剔除task是否全部剔除完成
            if (!queryDeletingTaskCount(apiCode, XcProcessTaskEnum.PROCESS_FALSE)) {
                threadPoolShutDown(threadPool);
                return;
            }
            //4.清洗流程 极端情况，多个剔除pod处理的batch同时完成，都会进入清洗流程，所以清洗流程拿数据也需要加锁
            cleanProcess(apiCode, threadPool);
            //5.关闭线程池
            threadPoolShutDown(threadPool);
        });
    }

    /**
     * @description 动态补充包剔除
     * @return void
     * @author hedongshuo
     * @date 2024/11/8 16:17
     **/
    @Override
    public void processDynaDelete() {
        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach((String apiCode) -> {
            //1.创建线程池
            ThreadPoolExecutor threadPool = getThreadPoolExecutor();
            //2.当天所有true剔除&清洗task是否全部完成
            if (!queryDeletingTaskCount(apiCode, XcProcessTaskEnum.PROCESS_DYNA_FALSE)) {
                threadPoolShutDown(threadPool);
                return;
            }
            //3.剔除流程
            deleteProcess(apiCode, XcProcessTaskEnum.PROCESS_DYNA_FALSE, threadPool);
            threadPoolShutDown(threadPool);
        });
    }

    /**
     * @description 清洗流程
     * @param apiCode
     * @param threadPool
     * @return void
     * @author hedongshuo
     * @date 2024/8/8 14:43
     **/
    private void cleanProcess(String apiCode, ThreadPoolExecutor threadPool) {
        String key = RedisKeyConstant.XIECHENG_COLLIDING_CLEAN.concat(":").concat(apiCode);
        String lockValue = UUID.randomUUID().toString();
        try {
            boolean lock = redisChgService.lock(key, lockValue, 5000L);
            if (lock) {
                if (queryUnCleanedTaskCount(apiCode)) {
                    //更新当日清洗task的status=1
                    updateTaskToCleaning(apiCode);
                    redisChgService.unlock(key, lockValue);
                    //清洗逻辑
                    cleanProcessForTasks(apiCode, threadPool);
                } else {
                    redisChgService.unlock(key, lockValue);
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程清洗抢锁出现异常，" + "errorMessage=" + e.getMessage()), e);
            redisChgService.unlock(key, lockValue);
        }
    }

    /**
     * @description 清洗逻辑
     * @param apiCode
     * @param threadPool
     * @return void
     * @author hedongshuo
     * @date 2024/8/8 17:28
     **/
    private void cleanProcessForTasks(String apiCode, ThreadPoolExecutor threadPool) {
        List<XiechengCollidingDataProcessTask> taskList = queryCleanTasks(apiCode);
        List<Long> newTaskIds = taskList.stream().map(XiechengCollidingDataProcessTask::getId).collect(Collectors.toList());
        taskList.forEach((XiechengCollidingDataProcessTask task) -> {
            cleanForTask(task, newTaskIds, threadPool);
        });
    }

    /**
     * @description 单条task清洗
     * @param task
     * @param newTaskIds
     * @param threadPool
     * @return void
     * @author hedongshuo
     * @date 2024/8/8 17:51
     **/
    private void cleanForTask(XiechengCollidingDataProcessTask task, List<Long> newTaskIds, ThreadPoolExecutor threadPool) {
        int actualNumber = 0;
        List<XieChengCollidingDataPackage> packages = getPackageByTaskId(task);
        if (!CollectionUtils.isEmpty(packages)) {
            actualNumber = cleanFalseData(task, packages, newTaskIds, threadPool);
        }
        updateTaskStatusAndActualNumber(task, actualNumber);
    }

    /**
     * @description 查询当日所有清洗task
     * @param apiCode
     * @return java.util.List<com.br.marketing.entity.XiechengCollidingDataProcessTask>
     * @author hedongshuo
     * @date 2024/8/8 17:32
     **/
    private List<XiechengCollidingDataProcessTask> queryCleanTasks(String apiCode) {
        XiechengCollidingDataProcessTaskExample processTaskExample = new XiechengCollidingDataProcessTaskExample();
        processTaskExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andTaskStartTimeGreaterThanOrEqualTo(getStartOfDate())
                .andTaskStartTimeLessThanOrEqualTo(new Date())
                .andTaskTypeEqualTo(0)
                .andTaskStatusEqualTo(1)
                .andIsDeleteEqualTo(0);
        processTaskExample.setOrderByClause("create_time asc");
        List<XiechengCollidingDataProcessTask> taskList = taskMapper.selectByExample(processTaskExample);
        return taskList;
    }

    /**
     * @description
     * @param apiCode
     * @return void
     * @author hedongshuo
     * @date 2024/8/8 17:20
     **/
    private void updateTaskToCleaning(String apiCode) {
        XiechengCollidingDataProcessTaskExample processTaskExample = new XiechengCollidingDataProcessTaskExample();
        processTaskExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andTaskStartTimeGreaterThanOrEqualTo(getStartOfDate())
                .andTaskStartTimeLessThanOrEqualTo(new Date())
                .andTaskTypeEqualTo(0)
                .andTaskStatusEqualTo(0)
                .andIsDeleteEqualTo(0);
        XiechengCollidingDataProcessTask processTask = new XiechengCollidingDataProcessTask();
        processTask.setTaskStatus(1);
        processTask.setUpdateTime(new Date());
        taskMapper.updateByExampleSelective(processTask, processTaskExample);
    }

    /**
     * @description 查询当日有无status=0的清洗task
     * @param apiCode
     * @return boolean
     * @author hedongshuo
     * @date 2024/8/8 17:20
     **/
    private boolean queryUnCleanedTaskCount(String apiCode) {
        XiechengCollidingDataProcessTaskExample processTaskExample = new XiechengCollidingDataProcessTaskExample();
        processTaskExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andTaskStartTimeGreaterThanOrEqualTo(getStartOfDate())
                .andTaskStartTimeLessThanOrEqualTo(new Date())
                .andTaskTypeEqualTo(0)
                .andTaskStatusEqualTo(0)
                .andIsDeleteEqualTo(0);
        int unCleanedTaskCount = taskMapper.countByExample(processTaskExample);
        if (unCleanedTaskCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * @param apiCode
     * @param xcProcessTaskEnum
     * @return void
     * @description 当天所有指定类型的task是否全部剔除完成
     * @author hedongshuo
     * @date 2024/8/8 14:33
     **/
    @Override
    public boolean queryDeletingTaskCount(String apiCode, XcProcessTaskEnum xcProcessTaskEnum) {
        List<Integer> taskTypes = null;
        if (xcProcessTaskEnum == XcProcessTaskEnum.PROCESS_FALSE) {
            taskTypes = Arrays.asList(XcProcessTaskEnum.PROCESS_DELETE.getTaskType());
        } else if (xcProcessTaskEnum == XcProcessTaskEnum.PROCESS_DYNA_FALSE) {
            taskTypes = Arrays.asList(XcProcessTaskEnum.PROCESS_DELETE.getTaskType(),
                    XcProcessTaskEnum.PROCESS_FALSE.getTaskType());
        } else if (xcProcessTaskEnum == XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE) {
            taskTypes = Arrays.asList(XcProcessTaskEnum.PROCESS_DELETE.getTaskType(),
                    XcProcessTaskEnum.PROCESS_FALSE.getTaskType(),
                    XcProcessTaskEnum.PROCESS_DYNA_FALSE.getTaskType());
        }
        XiechengCollidingDataProcessTaskExample processTaskExample = new XiechengCollidingDataProcessTaskExample();
        processTaskExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andTaskStartTimeGreaterThanOrEqualTo(getStartOfDate())
                .andTaskTypeIn(taskTypes)
                .andTaskStatusNotEqualTo(2)
                .andIsDeleteEqualTo(0);
        int deletingTaskCount = taskMapper.countByExample(processTaskExample);
        if (deletingTaskCount > 0) {
            return false;
        }
        return true;
    }

    /**
     * @description 获取当天0时，精确到秒
     * @return java.util.Date
     * @author hedongshuo
     * @date 2024/8/7 20:53
     **/
    private static Date getStartOfDate() {
        LocalDate localDate = LocalDate.now();
        Date nowDate = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        return nowDate;
    }

    /**
     * @param apiCode
     * @param xcProcessTaskEnum
     * @param threadPool
     * @return void
     * @description 剔除流程
     * @author hedongshuo
     * @date 2024/8/7 16:57
     **/
    private void deleteProcess(String apiCode, XcProcessTaskEnum xcProcessTaskEnum, ThreadPoolExecutor threadPool) {
        String key = RedisKeyConstant.prefix.concat(xcProcessTaskEnum.getDeleteRedisKey()).concat(":").concat(apiCode);
        for (; ; ) {
            String lockValue = UUID.randomUUID().toString();
            XiechengCollidingTaskBatchVo vo = null;
            try {
                //1.抢锁
                redisChgService.lock(key, lockValue);
                //2.查数据
                vo = taskBatchMapper.selectEarliestBatch(apiCode, getStartOfDate(), xcProcessTaskEnum.getBatchType());
                if (null == vo) {
                    redisChgService.unlock(key, lockValue);
                    break;
                }
                //3.更新数据
                updateBatchAndTask(vo);
                //4.释放锁
                redisChgService.unlock(key, lockValue);
                //5.剔除
                int batchCount = deleteForBatch(vo, threadPool);
                //6.数据状态更新
                processAfterDeleteForBatch(batchCount, vo);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程剔除流程出现异常，batchId="
                                + (vo == null ? "null" : vo.getId())
                                + "errorMessage=" + e.getMessage()), e);
                redisChgService.unlock(key, lockValue);
                break;
            }
        }
    }

    /**
     * 针对batch剔除后，数据状态更新及处理
     * @param batchCount
     * @param vo
     */
    private void processAfterDeleteForBatch(int batchCount, XiechengCollidingTaskBatchVo vo) {
        //1.更新当前batch数据
        XiechengCollidingTaskBatch taskBatch = new XiechengCollidingTaskBatch();
        taskBatch.setId(vo.getId());
        taskBatch.setStatus(2);
        taskBatch.setActualNumber(batchCount);
        taskBatch.setUpdateTime(new Date());
        taskBatchMapper.updateByPrimaryKeySelective(taskBatch);
        //2.若当前batch对应的task下的所有batch的status = 2，更新task的数据
        XiechengCollidingTaskBatchExample taskBatchExample = new XiechengCollidingTaskBatchExample();
        taskBatchExample.createCriteria()
                .andApiCodeEqualTo(vo.getApiCode())
                .andCollidingDataTaskIdEqualTo(vo.getCollidingDataTaskId()).andIsDeleteEqualTo(Constants.DATA_ISDELETE_NO);
        List<XiechengCollidingTaskBatch> batchList = taskBatchMapper.selectByExample(taskBatchExample);
        long deletingBatchCount = batchList.stream().filter(batch -> batch.getStatus() == 0 || batch.getStatus() == 1).count();
        if (deletingBatchCount == 0) {
            Integer actualNumber = batchList.stream().collect(Collectors.summingInt(XiechengCollidingTaskBatch::getActualNumber));
            XiechengCollidingDataProcessTask processTask = new XiechengCollidingDataProcessTask();
            processTask.setId(vo.getCollidingDataTaskId());
            processTask.setTaskStatus(2);
            processTask.setActualNumber(actualNumber);
            processTask.setTaskEndTime(new Date());
            processTask.setUpdateTime(new Date());
            taskMapper.updateByPrimaryKeySelective(processTask);
        }
    }

    /**
     * @param vo
     * @param threadPool
     * @return int
     * @description 以batch为维度，做剔除
     * @author hedongshuo
     * @date 2024/8/8 11:09
     **/
    private int deleteForBatch(XiechengCollidingTaskBatchVo vo, ThreadPoolExecutor threadPool) {
        Integer type = vo.getType();
        AtomicInteger batchCount = new AtomicInteger(0);
        String conditions = vo.getTaskExecutionConditions();
        String batchNumber = vo.getBatchNumber();
        String tableName = "b_xiecheng_colliding_" + batchNumber;
        String queryRuleScoreDataSql = "select id, cell, is_delete from "
                + tableName + " where " + conditions;
        String extend = "";
        String errorPrefix = "";
        if (type == XcProcessTaskEnum.PROCESS_DELETE.getBatchType()) {
            extend = EXTEND_DELETE_PREFIX + vo.getCollidingDataTaskId();
            errorPrefix = ERROR_PREFIX_DELETE;
        } else if (type == XcProcessTaskEnum.PROCESS_DYNA_FALSE.getBatchType()) {
            extend = EXTEND_DYNA_DELETE_PREFIX + vo.getCollidingDataTaskId();
            errorPrefix = ERROR_PREFIX_DYNA_DELETE;
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Long minId = null;
        Integer pageSize;
        Map<String, Integer> xieChengCollidingDataProcessPageSize = marketingCommonConfig.getXieChengCollidingDataProcessPageSize();
        if (null == xieChengCollidingDataProcessPageSize) {
            pageSize = PAGE_SIZE;
        } else {
            pageSize = xieChengCollidingDataProcessPageSize.getOrDefault("deletePageSize", PAGE_SIZE);
        }
        for(; ; ) {
            List<Long> longs = null;
            if (type == XcProcessTaskEnum.PROCESS_DELETE.getBatchType()) {
                    longs = cycleMapper.selectIdsOfTrueDataProcessTaskWithRangetikv_(
                            minId, queryRuleScoreDataSql, tableName, vo.getReleaseTimeBegin(), vo.getReleaseTimeEnd(), pageSize);
            } else if (type == XcProcessTaskEnum.PROCESS_DYNA_FALSE.getBatchType()) {
                longs = robMapper.selectIdsOfDynaFalseDataProcessTasktikv_(minId, queryRuleScoreDataSql, tableName, pageSize);
            }
            if (CollectionUtils.isEmpty(longs)) {
                break;
            }
            modifyThreadPool(threadPool);
            minId = longs.get(longs.size() - 1);
            List<List<Long>> partitions = Lists.partition(longs, PARTATION_SIZE);
            for (List<Long> partition : partitions) {
                String finalExtend = extend;
                String finalErrorPrefix = errorPrefix;
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        if (type == XcProcessTaskEnum.PROCESS_DELETE.getBatchType()) {
                            batchCount.addAndGet(cycleMapper.updateIsDeleteByIds(partition, finalExtend));
                        } else if (type == XcProcessTaskEnum.PROCESS_DYNA_FALSE.getBatchType()) {
                            batchCount.addAndGet(robMapper.updateBatchByIdToIsDeleted(partition, finalExtend));
                        }
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                                finalErrorPrefix + vo.getId() + "errorMessage=" + e.getMessage()), e);
                    }
                }, threadPool));
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return batchCount.get();
    }

    /**
     * @description
     * @param vo
     * @return void
     * @author hedongshuo
     * @date 2024/8/8 10:24
     **/
    private void updateBatchAndTask(XiechengCollidingTaskBatchVo vo) {
        Integer taskStatus = vo.getTaskStatus();
        if (2 == taskStatus) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程剔除任务状态异常，batchId=" + vo.getId()));
            return;
        }
        XiechengCollidingTaskBatch taskBatch = new XiechengCollidingTaskBatch();
        taskBatch.setId(vo.getId());
        taskBatch.setStatus(1);
        taskBatch.setUpdateTime(new Date());
        taskBatchMapper.updateByPrimaryKeySelective(taskBatch);
        if (0 == taskStatus) {
            XiechengCollidingDataProcessTask processTask = new XiechengCollidingDataProcessTask();
            processTask.setId(vo.getCollidingDataTaskId());
            processTask.setTaskStatus(1);
            processTask.setTaskExecuteTime(new Date());
            processTask.setUpdateTime(new Date());
            taskMapper.updateByPrimaryKeySelective(processTask);
        }
    }

    private ThreadPoolExecutor getThreadPoolExecutor() {
        Integer threadPoolSize = marketingCommonConfig.getXieChengCollidingDataProcessThread();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
        return threadPool;
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库数据清洗作业线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程撞库数据清洗作业，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
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

    /**
     * 修改线程池大小
     * @param pool
     */
    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getXieChengCollidingDataProcessThread();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
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
            log.warn("携程撞库数据清洗任务，大优先级旧包，最大撞库结束时间:{}", Joiner.on(",").join(maxEndTimeGroupByPackageId
                    .stream().map(XiechengCollidingDataPackageRule::getPackageId).collect(Collectors.toList())));

            // 获取清洗时间小于等于旧包最大结束时间的数据包
            reserveIds = getReserveIds(task, maxEndTimeGroupByPackageId);
        }
        log.warn("携程撞库数据清洗任务，旧包优先级大于等于新包优先级&&清洗时间小于等于旧包最大结束时间，数据包id:{}", Joiner.on(",").join(reserveIds));

        // 获取待删除的数据包id
        List<XieChengCollidingDataPackage> deletePackages =
                oldPackages.stream().filter(t -> !reserveIds.contains(t.getId())).collect(Collectors.toList());
        log.warn("携程撞库数据清洗任务，旧包待剔除数据的数据包:{}",
                Joiner.on(",").join(deletePackages.stream().map(XieChengCollidingDataPackage::getId).collect(Collectors.toList())));

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
        LocalDateTime cleanDateTime = task.getTaskStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        reserveIds =
                maxEndTimeGroupByPackageId.stream()
                        .filter((XiechengCollidingDataPackageRule t) -> t.getCollidingEndTime() != null)
                        .filter((XiechengCollidingDataPackageRule t) -> {
                            LocalDateTime collidingMaxDateTIme = t.getCollidingEndTime().toInstant()
                                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
                            return !cleanDateTime.isAfter(collidingMaxDateTIme);
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
                Long minId = null;
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
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                                    "携程撞库FALSE数据删除，单线程处理异常，taskId=" + task.getId() + "errorMessage=" + e.getMessage()), e);
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
        String conditions = task.getTaskExecutionConditions();
        Integer pageSize;
        Map<String, Integer> xieChengCollidingDataProcessPageSize = marketingCommonConfig.getXieChengCollidingDataProcessPageSize();
        if (null == xieChengCollidingDataProcessPageSize) {
            pageSize = PAGE_SIZE;
        } else {
            pageSize = xieChengCollidingDataProcessPageSize.getOrDefault("cleanPageSize", PAGE_SIZE);
        }
        for (String batchNumber : task.getBatchNumber().split(",")) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }
            String queryRuleScoreDataSql = "select id, cell, is_delete from b_xiecheng_colliding_" + batchNumber + " where " + conditions;
            log.warn("携程撞库数据清洗任务，新包新增数据查询条件:{}", queryRuleScoreDataSql);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            Long minId = null;
            while (true) {
                List<XieChengRuleScoreData> ruleScoreData = ruleScoreRecordMapper.selectRuleScoreDataExcludeTrueAndFalseDatatikv_(minId,
                        queryRuleScoreDataSql, pageSize);
                if (CollectionUtils.isEmpty(ruleScoreData)) {
                    break;
                }
                minId = ruleScoreData.get(ruleScoreData.size() - 1).getId();
                modifyThreadPool(threadPool);
                List<List<XieChengRuleScoreData>> partitions = Lists.partition(ruleScoreData, PARTATION_SIZE);
                for (List<XieChengRuleScoreData> partition : partitions) {
                    futures.add(CompletableFuture.runAsync(() -> insertData(partition, collidingDataPackage), threadPool));
                }
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
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程撞库FALSE数据插入，单线程处理异常：packageId=" + collidingDataPackage.getId()
                            + "errorMessage=" + e.getMessage()), e);
        }
    }
}
