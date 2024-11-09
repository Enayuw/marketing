package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.enums.XcProcessTaskEnum;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapper;
import com.br.marketing.mapper.XiechengCollidingTaskBatchMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.XiechengCollidingTaskBatchVo;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class XieChengPreCollidingBlackListDeleteServiceImpl implements XieChengPreCollidingBlackListDeleteService {
    private final static int PAGE_SIZE = 10000;

    private final static int PARTITION_SIZE = 2000;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Autowired
    RedisChgService redisChgService;
    @Resource
    XiechengCollidingDataProcessTaskMapper taskMapper;
    @Resource
    XiechengCollidingTaskBatchMapper taskBatchMapper;
    @Autowired
    private DingDingRobotHookService dingDingRobotHookService;
    @Resource
    XieChengCollidingDataLoopCycleMapper cycleMapper;
    @Resource
    XieChengCollidingDataRobMapper robMapper;

    @Override
    public void process() {
        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach((String apiCode) -> {
            //1.创建线程池
            Integer threadPoolSize = marketingCommonConfig.getXieChengCollidingDataProcessThread();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
            //2.当天所有动态包剔除任务是否全部完成
            if (!queryDeletingTaskCount(apiCode, XcProcessTaskEnum.PROCESS_DYNA_FALSE.getTaskType())) {
                threadPoolShutDown(threadPool);
                return;
            }
            //3.剔除流程
            deleteProcess(apiCode, XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE, threadPool);
            threadPoolShutDown(threadPool);
        });
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
                //5.公共黑名单剔除
                deleteForPublicBlacklists(vo, threadPool);
                //6自研AI业务黑名单/百应业务黑名单剔除
                deleteForNoPublicBlacklists(vo, threadPool);
                //7.数据状态更新
                processAfterDeleteForBatch(vo);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程黑名单剔除流程出现异常，batchId=" + vo.getId() + "errorMessage=" + e.getMessage()), e);
                redisChgService.unlock(key, lockValue);
            }
        }
    }

    /**
     * @Description:公共黑名单剔除
     * @Author: Ethan.Kang
     */
    private void deleteForPublicBlacklists(XiechengCollidingTaskBatchVo vo, ThreadPoolExecutor threadPool) {
        //公共黑名单周期表剔除
        deleteCycPublicBlack(vo, threadPool);
        //公共黑名单rob表剔除
        deleteRobPublicBlack(vo, threadPool);
    }

    /**
     * @Description:自研AI业务黑名单/百应业务黑名单剔除
     * @Author: Ethan.Kang
     */
    private void deleteForNoPublicBlacklists(XiechengCollidingTaskBatchVo vo, ThreadPoolExecutor threadPool) {
        //非公共黑名单周期表剔除(自研AI业务黑名单)
        deleteCycNoPublicBlackZY(vo, threadPool);
        //非公共黑名单周期表剔除(百应业务黑名单)
        deleteCycNoPublicBlackBY(vo, threadPool);
        //非公共黑名单rob表剔除(自研AI业务黑名单)
        deleteRobNoPublicBlackZY(vo, threadPool);
        //非公共黑名单rob表剔除(百应业务黑名单)
        deleteRobNoPublicBlackBY(vo, threadPool);
    }

    private void deleteCycNoPublicBlackBY(XiechengCollidingTaskBatchVo vo, ThreadPoolExecutor threadPool) {
        String conditions = vo.getTaskExecutionConditions();
        String batchNumber = vo.getBatchNumber();
        String tableName = "b_xiecheng_colliding_" + batchNumber;
        String queryRuleScoreDataSql = "select cell from "
                + tableName + " where " + conditions;
        Long minId = null;
        while (true) {
            List<Long> ids = cycleMapper.selectCycleNoPublicBlackListBYIdsByPage(minId, queryRuleScoreDataSql, tableName, PAGE_SIZE);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            List<List<Long>> partition = Lists.partition(ids, PARTITION_SIZE);
            for (List<Long> cycList : partition) {
                threadPool.submit(() -> {
                    try {

                        String extend = DateUtils.format(new Date()) + ":百应业务黑名单剔除";
                        cycleMapper.batchUpdateNoPublicBlackListBYData(cycList, extend);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新周期表百应黑名单状态，子线程处理异常"), e);
                    }
                });
            }

        }
    }

    private void deleteRobNoPublicBlackBY(XiechengCollidingTaskBatchVo vo, ThreadPoolExecutor threadPool) {
        String conditions = vo.getTaskExecutionConditions();
        String batchNumber = vo.getBatchNumber();
        String tableName = "b_xiecheng_colliding_" + batchNumber;
        String queryRuleScoreDataSql = "select cell from "
                + tableName + " where " + conditions;
        Long minId = null;
        while (true) {
            List<Long> ids = robMapper.selectRobNoPublicBlackListBYIdsByPage(minId, queryRuleScoreDataSql, tableName, PAGE_SIZE);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            List<List<Long>> partition = Lists.partition(ids, PARTITION_SIZE);
            for (List<Long> cycList : partition) {
                threadPool.submit(() -> {
                    try {
                        String extend = DateUtils.format(new Date()) + ":百应业务黑名单剔除";
                        robMapper.batchUpdateNoPublicBlackListBYData(cycList, extend);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新非周期表百应业务黑名单状态，子线程处理异常"), e);
                    }
                });
            }
        }
    }

    private void deleteCycNoPublicBlackZY(XiechengCollidingTaskBatchVo vo, ThreadPoolExecutor threadPool) {
        String conditions = vo.getTaskExecutionConditions();
        String batchNumber = vo.getBatchNumber();
        String tableName = "b_xiecheng_colliding_" + batchNumber;
        String queryRuleScoreDataSql = "select cell from "
                + tableName + " where " + conditions;
        Long minId = null;
        while (true) {
            List<Long> ids = cycleMapper.selectCycleNoPublicBlackListZYIdsByPage(minId, queryRuleScoreDataSql, tableName, PAGE_SIZE);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            List<List<Long>> partition = Lists.partition(ids, PARTITION_SIZE);
            for (List<Long> cycList : partition) {
                threadPool.submit(() -> {
                    try {

                        String extend = DateUtils.format(new Date()) + ":自研AI业务黑名单剔除";
                        cycleMapper.batchUpdateNoPublicBlackListZYData(cycList, extend);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新周期表自研AI黑名单状态，子线程处理异常"), e);
                    }
                });
            }

        }
    }

    private void deleteRobNoPublicBlackZY(XiechengCollidingTaskBatchVo vo, ThreadPoolExecutor threadPool) {
        String conditions = vo.getTaskExecutionConditions();
        String batchNumber = vo.getBatchNumber();
        String tableName = "b_xiecheng_colliding_" + batchNumber;
        String queryRuleScoreDataSql = "select cell from "
                + tableName + " where " + conditions;
        Long minId = null;
        while (true) {
            List<Long> ids = robMapper.selectRobNoPublicBlackListZYIdsByPage(minId, queryRuleScoreDataSql, tableName, PAGE_SIZE);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            List<List<Long>> partition = Lists.partition(ids, PARTITION_SIZE);
            for (List<Long> cycList : partition) {
                threadPool.submit(() -> {
                    try {

                        String extend = DateUtils.format(new Date()) + ":自研AI业务黑名单剔除";
                        robMapper.batchUpdateNoPublicBlackListZYData(cycList, extend);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新非周期表自研AI黑名单状态，子线程处理异常"), e);
                    }
                });
            }
        }
    }


    private void deleteCycPublicBlack(XiechengCollidingTaskBatchVo vo, ThreadPoolExecutor threadPool) {
        Long minId = null;
        while (true) {
            List<Long> ids = cycleMapper.selectCycleBlackListIdsByPage(minId, PAGE_SIZE);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            List<List<Long>> partition = Lists.partition(ids, PARTITION_SIZE);
            for (List<Long> cycList : partition) {
                threadPool.submit(() -> {
                    try {

                        String extend = DateUtils.format(new Date()) + ":公共黑名单剔除";
                        cycleMapper.batchUpdateBlackListData(cycList, extend);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新周期表黑名单状态，子线程处理异常"), e);
                    }
                });
            }
        }
    }

    private void deleteRobPublicBlack(XiechengCollidingTaskBatchVo vo, ThreadPoolExecutor threadPool) {
        Long minId = null;
        while (true) {
            List<Long> ids = robMapper.selectCycleBlackListIdsByPage(minId, PAGE_SIZE);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            List<List<Long>> partition = Lists.partition(ids, PARTITION_SIZE);
            for (List<Long> cycList : partition) {
                threadPool.submit(() -> {
                    try {

                        String extend = DateUtils.format(new Date()) + ":公共黑名单剔除";
                        robMapper.batchUpdateBlackListData(cycList, extend);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新非周期表黑名单状态，子线程处理异常"), e);
                    }
                });
            }
        }
    }

    private void processAfterDeleteForBatch(XiechengCollidingTaskBatchVo vo) {
        //1.更新当前batch数据
        XiechengCollidingTaskBatch taskBatch = new XiechengCollidingTaskBatch();
        taskBatch.setId(vo.getId());
        taskBatch.setStatus(2);
        taskBatch.setUpdateTime(new Date());
        taskBatchMapper.updateByPrimaryKeySelective(taskBatch);
        //2.若当前batch对应的task下的所有batch的status = 2，更新task的数据
        XiechengCollidingTaskBatchExample taskBatchExample = new XiechengCollidingTaskBatchExample();
        taskBatchExample.createCriteria()
                .andApiCodeEqualTo(vo.getApiCode())
                .andCollidingDataTaskIdEqualTo(vo.getCollidingDataTaskId());
        List<XiechengCollidingTaskBatch> batchList = taskBatchMapper.selectByExample(taskBatchExample);
        long deletingBatchCount = batchList.stream().filter(batch -> batch.getStatus() == 0 || batch.getStatus() == 1).count();
        if (deletingBatchCount == 0) {
            XiechengCollidingDataProcessTask processTask = new XiechengCollidingDataProcessTask();
            processTask.setId(vo.getCollidingDataTaskId());
            processTask.setTaskStatus(2);
            processTask.setTaskEndTime(new Date());
            processTask.setUpdateTime(new Date());
            taskMapper.updateByPrimaryKeySelective(processTask);
            String msg = "携程撞库周期TRUE数据删除量级:" + 11;
            Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
            Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.XIECHENG_TRUE_DELETE_NOTICE.toString());
            dingDingRobotHookService.sendDingDingTextMessage(msg, map);
        }
    }

    private void updateBatchAndTask(XiechengCollidingTaskBatchVo vo) {
        Integer taskStatus = vo.getTaskStatus();
        if (2 == taskStatus) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程黑名单剔除任务状态异常，batchId=" + vo.getId()));
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
            processTask.setUpdateTime(new Date());
            taskMapper.updateByPrimaryKeySelective(processTask);
        }
    }


    /**
     * @param apiCode
     * @param taskType
     * @return void
     * @description 当天所有指定类型的task是否全部剔除完成
     * @author KP
     * @date 2024/8/8 14:33
     **/
    private boolean queryDeletingTaskCount(String apiCode, Integer taskType) {
        XiechengCollidingDataProcessTaskExample processTaskExample = new XiechengCollidingDataProcessTaskExample();
        processTaskExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andTaskStartTimeEqualTo(getStartOfDate())
                .andTaskTypeEqualTo(taskType)
                .andTaskStatusNotEqualTo(2);
        int deletingTaskCount = taskMapper.countByExample(processTaskExample);
        if (deletingTaskCount > 0) {
            return false;
        }
        return true;
    }

    /**
     * @return java.util.Date
     * @description 获取当天0时，精确到秒
     * @author KP
     * @date 2024/8/7 20:53
     **/
    private static Date getStartOfDate() {
        LocalDate localDate = LocalDate.now();
        Date nowDate = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        return nowDate;
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库数据黑名单剔除作业线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程撞库数据黑名单剔除作业，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }


}
