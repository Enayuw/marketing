package com.br.marketing.monkey.job.umeng;


import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.UMengData;
import com.br.marketing.entity.UMengTimingTask;
import com.br.marketing.service.Impl.umeng.IUMengDataCallbackService;
import com.br.marketing.service.Impl.umeng.IUMengDataService;
import com.br.marketing.service.Impl.umeng.IUMengTimingTaskService;
import com.br.marketing.service.LocalFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 友盟设备注册job
 * @Author zhiyong.zhang
 * @CreateTime 2025/05/
 * @deprecated 业务下线  2025/12/23
 */
@Deprecated
@Component
@Slf4j
public class UMengCallPolicyJob extends AbstractSimpleElasticJob {
    private final static String TITLE = "【uMeng-推决策任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private IUMengTimingTaskService timingTaskService;

    @Resource
    private IUMengDataService uMengdataService;

    @Resource
    private IUMengDataCallbackService dataCallbackService;

    @Resource
    private LocalFileService localFileService;


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            String dealDate = shardingContext.getJobParameter();
            List<String> uMengApiCodes = marketingCommonConfig.getApiCodeOfUMeng();
            uMengApiCodes.forEach(apiCode -> action(apiCode,dealDate));
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.UMENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    private void action(String apiCode,String dealDate) {
        log.warn("TITLE:{},apiCode:{},dealDate:{}",TITLE,apiCode,dealDate);
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        LocalDateTime dayStartTime = LocalDate.now(zone).atStartOfDay();
        LocalDateTime dayEndTime = LocalDate.now(zone).atTime(LocalTime.MAX);
        if (StringUtils.isNotBlank(dealDate)) {
            LocalDate date = LocalDate.parse(dealDate);
            dayStartTime = date.atStartOfDay();
            dayEndTime = date.atTime(LocalTime.MAX);
        }
        //1、查询T日 b_local_file处理完成 记录
        List<LocalFile> localFileList = localFileService.getLastDataByApiCode(apiCode,dayStartTime,dayEndTime);
        localFileList.forEach(localFile -> dealSingleAction(localFile,apiCode));
    }

    private void dealSingleAction(LocalFile localFile, String apiCode) {
        log.warn("TITLE:{},localId:{},apiCode:{} 开始推决策",TITLE,localFile.getId(),apiCode);
        //2、查询T日智能时机任务创建记录
        UMengTimingTask timingTask = timingTaskService.getTodayLastTask(localFile.getId(),apiCode);
        if (timingTask == null) {
            log.warn("TITLE:{},localId:{},apiCode={} 今日智能时机任务未创建",TITLE,localFile.getId(),apiCode);
            return;
        }
        //3、查询未推决策 数据信息
        boolean checkCallbackEndFlag = checkCallBackEnd(marketingCommonConfig.getUMengCallBackEndTime());
        Long lastSearchId = 0L;
        Long totalCount = 0L;
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                marketingCommonConfig.getUMengCallPolicyPool(),
                marketingCommonConfig.getUMengCallPolicyPool());
        List<CompletableFuture<Result>> futureList = new ArrayList<>();
        List<Long> resultList = new ArrayList<>(20);
        while (true) {
            List<UMengData> uMengDataList = new ArrayList<>();
            Integer searchSize = marketingCommonConfig.getUMengCallPolicyPageSize();
            if (checkCallbackEndFlag) {
                uMengDataList = uMengdataService.selectEventPushList(localFile.getId(),apiCode,lastSearchId,searchSize);
            }else {
                uMengDataList = uMengdataService.selectDevicePushList(localFile.getId(),apiCode,lastSearchId,searchSize);
            }
            totalCount = totalCount + uMengDataList.size();
            if (CollectionUtils.isEmpty(uMengDataList)) {
                break;
            }
            //4、推决策
            dealCallPolicy(timingTask,uMengDataList,actionPool,futureList,resultList);
            lastSearchId = uMengDataList.get(uMengDataList.size()-1).getId();
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        shutdownThreadPool(actionPool);
        Long successLine = 0L;
        for (Long successCount : resultList) {
            successLine += successCount;
        }
        log.warn("TITLE:{},localId:{},apiCode:{}, callPolicy end,totalCount:{},successLine:{}",
                TITLE,timingTask.getLocalId(),apiCode,totalCount,successLine);
    }

    private Result dealCallPolicy(UMengTimingTask timingTask ,List<UMengData> uMengDataList,
                                  ThreadPoolExecutor actionPool,List<CompletableFuture<Result>> futureList,
                                  List<Long> resultList ) {
        Result result = new Result().failure();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(actionPool, marketingCommonConfig.getUMengCallPolicyPool());
        futureList.add(CompletableFuture.supplyAsync(() -> processCallPolicy(timingTask,uMengDataList), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (processDataResult == null || !processDataResult.isSuccess()) {
                        resultList.add(0L);
                        return;
                    }
                    resultList.add(Long.parseLong(processDataResult.getData().toString()));
                    if (throwable != null) {
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.UMENG_SERVICEERROR.getCode(),
                                throwable.getMessage(), TITLE), throwable);
                        resultList.add(0L);
                    }
                })
        );
        return result;
    }

    private Result processCallPolicy(UMengTimingTask timingTask,List<UMengData> uMengDataList) {
        Result result = new Result().failure();
        String strategyCode = marketingCommonConfig.getUMengPushPolicyStrategyCode().get(timingTask.getApiCode());
        List<List<UMengData>> partitionList = ListUtils.partition(uMengDataList, marketingCommonConfig.getUMengPolicyPartCount());
        for (List<UMengData> partitionItemList : partitionList) {
            Result pushResult = dataCallbackService.callPolicyData(timingTask.getLocalId(),timingTask.getApiCode(),strategyCode,partitionItemList);
            if (pushResult != null && pushResult.isSuccess()) {
                List<Long> idList = partitionItemList.stream().map(UMengData::getId).collect(Collectors.toList());
                uMengdataService.updatePushStausByIds(idList,2);
            }
        }
        return result.success().setDate(uMengDataList.size());
    }

    public  void shutdownThreadPool(ThreadPoolExecutor executor) {
        log.warn(TITLE + "shutdownThreadPool开始");
        long taskCount = -1;
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                long completedTaskCount = executor.getCompletedTaskCount();
                if (taskCount == completedTaskCount) {
                    log.warn(TITLE + "业务线程等待超时");
                    break;
                }
                taskCount = completedTaskCount;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            log.warn(TITLE + "ThreadPoolManager shutdown executor has error : ", e);
        }
        log.warn(TITLE + "shutdownThreadPool结束");
    }

    /**
     * 只比较小时时间
     * @param callBackEndTimeStr
     * @return
     */
    private boolean checkCallBackEnd(String callBackEndTimeStr) {
        LocalTime nowTime = LocalTime.now();
        LocalTime callBackTime = LocalTime.parse(callBackEndTimeStr);
        return nowTime.isBefore(callBackTime);
    }
}
