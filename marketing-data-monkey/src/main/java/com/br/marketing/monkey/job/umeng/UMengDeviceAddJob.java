package com.br.marketing.monkey.job.umeng;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.UMengData;
import com.br.marketing.entity.UMengTimingTask;
import com.br.marketing.service.Impl.umeng.IUMengApiService;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 友盟设备注册job
 * @Author zhiyong.zhang
 * @CreateTime 2025/05/23
 * @deprecated 业务下线  2025/12/23
 */
@Deprecated
@Component
@Slf4j
public class UMengDeviceAddJob extends AbstractSimpleElasticJob {
    private final static String TITLE = "【uMeng-设备注册任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private IUMengTimingTaskService timingTaskService;

    @Resource
    private IUMengApiService uMengApiService;

    @Resource
    private IUMengDataService uMengdataService;

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
        //1、查询T日 b_local_file(list)处理完成 记录
        List<LocalFile> localFileList = localFileService.getLastDataByApiCode(apiCode,dayStartTime,dayEndTime);
        localFileList.forEach(localFile -> dealSingleAction(localFile,apiCode));
    }

    private void dealSingleAction(LocalFile localFile, String apiCode) {
        log.warn("TITLE:{},localId:{},apiCode:{} 开始进行设备注册",TITLE,localFile.getId(),apiCode);
        //2、查询T日智能时机任务创建记录
        UMengTimingTask timingTask = timingTaskService.getTodayLastTask(localFile.getId(),apiCode);
        if (timingTask == null || !checkExpireTime(timingTask)) {
            log.warn("TITLE:{},localId:{},apiCode={} 今日智能时机任务不存在或任务刚创建不到5分钟 ",TITLE,localFile.getId(),apiCode);
            return;
        }
        //3、查询未进行设备注册的 数据信息
        Long lastSearchId = 0L;
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                marketingCommonConfig.getUMengDeviceAddPool(),
                marketingCommonConfig.getUMengDeviceAddPool());
        List<CompletableFuture<Result>> futureList = new ArrayList<>();
        while (true) {
            List<UMengData> uMengDataList = uMengdataService.selectDeviceAddList(localFile.getId(),apiCode,
                    lastSearchId,marketingCommonConfig.getUMengDeviceAddPageSize());
            if (CollectionUtils.isEmpty(uMengDataList)) {
                break;
            }
            deviceAdd(uMengDataList,timingTask,actionPool,futureList);
            lastSearchId = uMengDataList.get(uMengDataList.size()-1).getId();
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        shutdownThreadPool(actionPool);
    }

    private void deviceAdd(List<UMengData> uMengDataList, UMengTimingTask timingTask,
                           ThreadPoolExecutor actionPool,List<CompletableFuture<Result>> futureList) {
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(actionPool, marketingCommonConfig.getUMengDeviceAddPool());
        futureList.add(CompletableFuture.supplyAsync(() -> processDeviceAdd(timingTask,uMengDataList), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (throwable != null) {
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.UMENG_SERVICEERROR.getCode(),throwable.getMessage(), TITLE), throwable);
                    }
                })
        );
    }

    private Result processDeviceAdd(UMengTimingTask timingTask, List<UMengData> uMengDataList) {
        Result result = new Result().failure();
        List<List<UMengData>> partitionList = ListUtils.partition(uMengDataList, marketingCommonConfig.getUMengDevicePartCount());
        for (List<UMengData> partitionItemList : partitionList) {
            Integer deviceAddStatus =-1;
            List<Long> idList = partitionItemList.stream().map(UMengData::getId).collect(Collectors.toList());
            try{
                JSONObject requestParam = buildRequestParam(timingTask,partitionItemList);
                Result deviceAddResult = uMengApiService.deviceAdd(timingTask.getLocalId(),timingTask.getApiCode(),requestParam.toJSONString(),true);
                if (deviceAddResult != null  && deviceAddResult.isSuccess()) {
                    JSONObject resultObj = (JSONObject) deviceAddResult.getData();
                    if (resultObj != null && resultObj.getBoolean("status")) {
                        deviceAddStatus = 2;
                    }
                }
            }catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.UMENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            }finally {
                uMengdataService.updateDeviceAddStatus(idList,deviceAddStatus);
            }
        }
        return result.success();
    }

    /**
     * 构建设备注册参数
     * eg
     * {
     *     "task_ids": [
     *         "sqg11746598977wccplxl4rx"
     *     ],
     *     "device_list": [
     *         {
     *             "phone_md5": "",
     *             "phone_sha256": "0000111b93e70fd2d173333ce988da0cc65dbd18678ssss20ad8380fab2fbd07",
     *             "imei_md5": "",
     *             "oaid_md5": "",
     *             "idfa_md5": "",
     *             "aaid": "sqg11746598977wccp",
     *             "appkey": "b9073f30a7837609b97f5ce0e962d42e",
     *             "device_token": "",
     *             "umid": "",
     *             "custom_param": ""
     *         }
     *     ]
     * }
     * @param timingTask
     * @param partitionItemList
     * @return
     */
    private JSONObject buildRequestParam(UMengTimingTask timingTask, List<UMengData> partitionItemList) {
        String bizId = marketingCommonConfig.getUMengBizInfoMap().get("bizId");
        String bizSecret = marketingCommonConfig.getUMengBizInfoMap().get("bizSecret");
        JSONObject requestParam = new JSONObject();
        JSONArray taskIdArray = new JSONArray();
        taskIdArray.add(timingTask.getUmengTaskId());
        requestParam.put("task_ids",taskIdArray);
        JSONArray deviceArray = new JSONArray();
        partitionItemList.forEach(partitionItem -> {
            JSONObject deviceItem = new JSONObject();
            deviceItem.put("aaid",bizId);
            deviceItem.put("appkey",bizSecret);
            deviceItem.put("phone_sha256",partitionItem.getCell());
            deviceArray.add(deviceItem);
        });
        requestParam.put("device_list",deviceArray);
        return requestParam;
    }

    private boolean checkExpireTime(UMengTimingTask timingTask) {
        boolean flag = false;
        if (timingTask == null) {
            return false;
        }
        Date now = new Date();
        long differenceInMillis = now.getTime() - timingTask.getCreateTime().getTime();
        if (differenceInMillis > 5 * 60 * 1000) { // 5 minutes in milliseconds
            flag = true;
        }
        return flag;
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

}
