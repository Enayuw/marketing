package com.br.marketing.bridge.job.tc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.enums.TcSyncRecordStatusEnum;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.tc.TcSyncDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 同程易融上传数据清洗任务
 * @Author zhiyong.zhang
 * @CreateTime 2025/04/21
 */
@Component
@Slf4j
public class TcSyncDataCleanJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融-上传数据清洗任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataCleanService tcSyncDataCleanService;

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Resource
    private PushInfoService pushInfoService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE+"调度开始");
            atciton(marketingCommonConfig.getTcyrApiCode());
            log.warn(TITLE+"调度结束");
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    /**
     * 具体执行动作
     * 1、查询未执行任务list
     * 2、单个批次号batchNo任务执行操作:
     *    (1)查询 b_marketing_tcyr_sync_record 接入成功的数据
     *    (2)查找 b_marketing_tcyr_sync (batch_no = record.data.batchNo，is_clean = 0，limit 1000)
     *    (3)调用接口uploadClean(List<Object>, apiCode),
     *    (4)uploadClean成功，修改b_marketing_tcyr_syn is_clean=1
     *    (5)修改batchNo 对应 b_marketing_tcyr_sync_record 状态 ->清洗完成
     * @param apiCode
     */
    private void atciton(String apiCode) {
        List<MarketingTcyrSyncRecord> syncRecordList = tcSyncDataCleanService.searchAllTcyrSyncList(apiCode, TcSyncRecordStatusEnum.ACCESS_SUCCESS.getValue());

        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(10, 10);
        List<CompletableFuture<Result>> futureList = new ArrayList<>();
        List<Long> resultList = Collections.synchronizedList(new ArrayList<>(20));

        for (MarketingTcyrSyncRecord syncRecord : syncRecordList) {
            try {
                boolean stillFlag =true;
                Long lastSearchId =0L;
                Integer searchSize = marketingCommonConfig.getTcPageSearchSize();
                while (stillFlag) {
                    List<MarketingTcyrSync> tcyrSyncList = tcSyncDataCleanService.selectTcSyncList(syncRecord.getBatchNo(),0,lastSearchId,searchSize);
                    if (CollectionUtils.isEmpty(tcyrSyncList)) {
                        stillFlag = false;
                    }else {
                        if (tcyrSyncList.size() < searchSize) {
                            stillFlag = false;
                        }else {
                            lastSearchId = tcyrSyncList.get(tcyrSyncList.size()-1).getId();
                        }
                        processList(syncRecord.getApiCode(),syncRecord.getBatchNo(),tcyrSyncList,actionPool,futureList,resultList);
                    }
                }
                CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
                Long successLine = 0L;
                for (Long successCount : resultList) {
                    successLine += successCount;
                }
                log.warn("{},apiCode:{},batchNo:{} syncDataClean process complete,successLine:{}",TITLE,syncRecord.getApiCode(),syncRecord.getBatchNo(),successLine);
            }catch (Exception e) {
                log.error("{} apiCode:{}, batchNo:{} syncDataClean异常,error: ",TITLE,syncRecord.getApiCode(),syncRecord.getBatchNo(),e);
            }
        }
    }


    private Result processList(String apiCode, String batchNo, List<MarketingTcyrSync> tcyrSyncList, ThreadPoolExecutor actionPool, List<CompletableFuture<Result>> futureList, List<Long> resultList) {
        Result result = new Result().failure();
        actionPool.setCorePoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        actionPool.setMaximumPoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        futureList.add(CompletableFuture.supplyAsync(() -> processData(apiCode, batchNo, tcyrSyncList), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (processDataResult == null || !processDataResult.isSuccess()) {
                        resultList.add(0L);
                        return;
                    }
                    resultList.add(Long.parseLong(processDataResult.getData().toString()));
                    if (throwable != null) {
                        log.error(TITLE + "completableFuture error:{}", throwable);
                        resultList.add(0L);
                    }
                })
        );
        return result.success();
    }

    private Result processData(String apiCode, String batchNo, List<MarketingTcyrSync> tcyrSyncList) {
        Result result = new Result().failure();
        try {
            List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(tcyrSyncList), JSONObject.class);
            Result callResult = generalDataCleanService.uploadClean(jsonObjectList, apiCode);
            if (callResult!=null && callResult.isSuccess()) {
                //调用定制化上传接口
                List<MarketingPreUserDetailDTO> marketingPreUserDetailDTOS = (List<MarketingPreUserDetailDTO>) callResult.getData();
                UploadDataDTO uploadDataDTO = initUploadData(apiCode,batchNo, marketingPreUserDetailDTOS);
                Result<Boolean> pullResult = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                // 修改状态为已清洗
                List<Long> idList =tcyrSyncList.stream().map(MarketingTcyrSync::getId).collect(Collectors.toList());
                tcSyncDataCleanService.updateCleanStatus(idList,1);
            }
            log.warn("{},batchNo:{} sycnDataClean成功,successLine:{}",TITLE,batchNo,tcyrSyncList.size());
            return result.success().setDate(tcyrSyncList.size());
        } catch (Exception e) {
            log.error(TITLE + "processData error", e);
            return result.failure();
        }
    }

    /**
     * 封装异步调用上传的数据
     *
     * @param apiCode   apiCode
     * @param syncUsers 具体数据对象
     */
    private UploadDataDTO initUploadData(String apiCode,String batchNo, List<MarketingPreUserDetailDTO> syncUsers) {
        String taskId = batchNo;
        String requestId = apiCode+"_"+taskId+"_"+System.currentTimeMillis();
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(taskId);
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

}
