package com.br.marketing.service.Impl.xc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.*;
import com.br.marketing.enums.XcProcessTaskEnum;
import com.br.marketing.enums.XieChengBlackListEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class XieChengPreCollidingBlackListDeleteServiceImpl implements XieChengPreCollidingBlackListDeleteService {

    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    XiechengCollidingDataProcessTaskMapper taskMapper;
    @Resource
    XieChengBlackListMapper blackListMapper;
    @Resource
    XieChengCollidingDataProcessService xieChengCollidingDataProcessService;

    @Override
    public void process() {
        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach((String apiCode) -> {
            //1.当天所有动态包剔除任务是否全部完成
            if (!xieChengCollidingDataProcessService.queryDeletingTaskCount(apiCode, XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE)) {
                return;
            }
            //2.剔除流程
            deleteProcess(apiCode, XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE);
        });
    }

    /**
     * @param apiCode
     * @param xcProcessTaskEnum
     * @description 剔除流程
     **/
    private void deleteProcess(String apiCode, XcProcessTaskEnum xcProcessTaskEnum) {
        XiechengCollidingDataProcessTaskExample example = new XiechengCollidingDataProcessTaskExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andTaskStatusEqualTo(0).andTaskTypeEqualTo(xcProcessTaskEnum.getTaskType()).
                andTaskStartTimeEqualTo(getStartOfDate()).andIsDeleteEqualTo(0);
        List<XiechengCollidingDataProcessTask> tasks = taskMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory
                .getThreadPool(ThreadPoolNameEnum.XIECHENG_BLACK_DELETE.getName(), 50, 100);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try {
            for (XiechengCollidingDataProcessTask task : tasks) {
                //1、携程撞库黑名单任务剔除开始,更新task任务状态
                processBeforeDeleteForBatch(task);
                //2.黑名单剔除
                deleteBlackList(task, today, threadPool, futures);
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                //3.更新task状态
                processAfterDeleteForBatch(task);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程黑名单剔除流程出现异常"), e);
        } finally {
            threadPool.shutdownAndAwaitTermination();
        }
    }

    private void deleteBlackList(XiechengCollidingDataProcessTask task,
                                 String today, TpDynamicExecutor threadPool,
                                 List<CompletableFuture<Void>> futures) {
        log.warn("携程黑名单剔除-opt");
        //1.公共黑名单
        //1.1周期剔除
        deleteWithBatchNumber(null, null, XieChengBlackListEnum.PUBLIC_BLACKLISTS,
                "b_xiecheng_colliding_data_loop_cycle", today, threadPool, futures);
        //1.2非周期剔除
        deleteWithBatchNumber(null, null, XieChengBlackListEnum.PUBLIC_BLACKLISTS,
                "b_xiecheng_colliding_data_rob", today, threadPool, futures);
        //2.自研/百应黑名单
        String condition = task.getTaskExecutionConditions();
        if (StringUtils.isBlank(condition)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程批量更新分组黑名单剔除，条件为空"));
            return;
        }
        List<String> batchNumbers = Arrays.asList(task.getBatchNumber().split(","));
        for (String batchNumber : batchNumbers) {
            deleteWithBatchNumber(batchNumber, condition, XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST,
                    "b_xiecheng_colliding_data_loop_cycle", today, threadPool, futures);
            deleteWithBatchNumber(batchNumber, condition, XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST,
                    "b_xiecheng_colliding_data_rob", today, threadPool, futures);
            deleteWithBatchNumber(batchNumber, condition, XieChengBlackListEnum.BAIYING_BUSINESS_BLACKLIST,
                    "b_xiecheng_colliding_data_loop_cycle", today, threadPool, futures);
            deleteWithBatchNumber(batchNumber, condition, XieChengBlackListEnum.BAIYING_BUSINESS_BLACKLIST,
                    "b_xiecheng_colliding_data_rob", today, threadPool, futures);
        }
    }

    /**
     * 黑名单剔除
     * @param batchNumber
     * @param condition
     * @param xieChengBlackListEnum
     * @param tableName
     * @param today
     * @param threadPool
     * @param futures
     */
    private void deleteWithBatchNumber(String batchNumber, String condition,
                                       XieChengBlackListEnum xieChengBlackListEnum,
                                       String tableName, String today,
                                       TpDynamicExecutor threadPool, List<CompletableFuture<Void>> futures) {
        String extend = today + "-" + xieChengBlackListEnum.getDesc();
        Long minId = null;
        List<Long> ids;
        for (; ; ) {
            try {
                ids = blackListMapper.selectIdsByBatchNumberAndCondition
                        (tableName, xieChengBlackListEnum.getValue(), batchNumber, condition, minId);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                        , "携程黑名单剔除-查询异常"), e);
                break;
            }
            if(CollectionUtils.isEmpty(ids)){
                break;
            }
            minId = ids.get(ids.size() - 1);
            List<Long> finalIds = ids;
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    blackListMapper.updateIsDeleteByIds(tableName, finalIds, extend);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage(),
                            String.format("携程黑名单剔除-更新异常, 参数: label_type=%d, tableName=%s, ids=%s, extend=%s",
                                    xieChengBlackListEnum.getValue(),
                                    tableName,
                                    finalIds,
                                    extend
                            )
                    ), e);
                }
            }, threadPool));
        }
    }



    private void processAfterDeleteForBatch(XiechengCollidingDataProcessTask vo) {
        XiechengCollidingDataProcessTask processTask = new XiechengCollidingDataProcessTask();
        processTask.setId(vo.getId());
        processTask.setTaskStatus(2);
        processTask.setTaskEndTime(new Date());
        processTask.setUpdateTime(new Date());
        taskMapper.updateByPrimaryKeySelective(processTask);
    }

    private void processBeforeDeleteForBatch(XiechengCollidingDataProcessTask task) {
        XiechengCollidingDataProcessTask entity = new XiechengCollidingDataProcessTask();
        entity.setId(task.getId());
        entity.setTaskStatus(1);
        entity.setUpdateTime(new Date());
        taskMapper.updateByPrimaryKeySelective(entity);
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

}
