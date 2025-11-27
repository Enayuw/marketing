package com.br.marketing.bridge.job.tc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
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
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
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
     *    (3)调用uploadClean方法(List<Object>, apiCode)
     *    (4)调用定制化上传接口
     *    (5) 3、4都成功后，修改b_marketing_tcyr_syn is_clean=1
     * @param apiCode
     */
    private void atciton(String apiCode) {
        List<MarketingTcyrSyncRecord> syncRecordList = tcSyncDataCleanService.searchAllTcyrSyncList(apiCode, TcSyncRecordStatusEnum.ACCESS_SUCCESS.getValue());

        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_DATA_CLEAN.getName(), 10, 10);
        List<CompletableFuture<Result>> futureList = new ArrayList<>();
        List<Long> resultList = new ArrayList<>(20);
        try {
            for (MarketingTcyrSyncRecord syncRecord : syncRecordList) {
                try {
                    Long lastSearchId =0L;
                    while (true) {
                        Integer searchSize = marketingCommonConfig.getTcPageSearchSize();
                        List<MarketingTcyrSync> tcyrSyncList = tcSyncDataCleanService.selectTcSyncList(syncRecord.getBatchNo(),0,lastSearchId,searchSize);
                        if (CollectionUtils.isEmpty(tcyrSyncList)) {
                            break;
                        }
                        //07-25 id-auto_random模式，批处理数据修改为中间态5
                        List<Long> idList =tcyrSyncList.stream().map(MarketingTcyrSync::getId).collect(Collectors.toList());
                        tcSyncDataCleanService.updateCleanStatus(idList,5);
                        processList(syncRecord.getApiCode(),syncRecord.getBatchNo(),tcyrSyncList,actionPool,futureList,resultList);
                        lastSearchId = tcyrSyncList.get(tcyrSyncList.size()-1).getId();
                    }
                    CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
                    Long successLine = 0L;
                    for (Long successCount : resultList) {
                        successLine += successCount;
                    }
                    log.warn("{},apiCode:{},batchNo:{} syncDataClean process complete,successLine:{}",TITLE,syncRecord.getApiCode(),syncRecord.getBatchNo(),successLine);
                }catch (Exception e) {
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
                }
            }
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }


    private Result processList(String apiCode, String batchNo, List<MarketingTcyrSync> tcyrSyncList, ThreadPoolExecutor actionPool, List<CompletableFuture<Result>> futureList, List<Long> resultList) {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(tcyrSyncList)) {
            return result.success();
        }
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(actionPool, marketingCommonConfig.getTcGzBatDBThreadPool());
        futureList.add(CompletableFuture.supplyAsync(() -> processData(apiCode, batchNo, tcyrSyncList), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (processDataResult == null || !processDataResult.isSuccess()) {
                        resultList.add(0L);
                        return;
                    }
                    resultList.add(Long.parseLong(processDataResult.getData().toString()));
                    if (throwable != null) {
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),throwable.getMessage(), TITLE), throwable);
                        resultList.add(0L);
                    }
                })
        );
        return result.success();
    }

    /**
     * is_clean 清洗状态 0-待清洗；1-清洗完成2:上传清洗失败 3:推送清洗失败 4:整体推送异常
     * @param apiCode
     * @param batchNo
     * @param tcyrSyncList
     * @return
     */
    private Result processData(String apiCode, String batchNo, List<MarketingTcyrSync> tcyrSyncList) {
        Result result = new Result().failure();
        List<List<MarketingTcyrSync>> partitionList = ListUtils.partition(tcyrSyncList, 1000);
        for(List<MarketingTcyrSync> tcyrSyncItemList : partitionList){
            List<Long> idList =tcyrSyncItemList.stream().map(MarketingTcyrSync::getId).collect(Collectors.toList());
            try {
                    //剔除column_1、column_2无用字段
                    for (MarketingTcyrSync tcyrSyncItem : tcyrSyncItemList) {
                        JSONObject extendJson = JSONObject.parseObject(tcyrSyncItem.getExtend());
                        List<String> tcyrSyncExcludeFieldList = marketingCommonConfig.getTcyrSyncCleanExcludeFieldList();
                        for (String excludeField : tcyrSyncExcludeFieldList) {
                            extendJson.remove(excludeField);
                        }
                        tcyrSyncItem.setExtend(extendJson.toJSONString());
                    }
                    List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(tcyrSyncItemList), JSONObject.class);
                    Result callResult = generalDataCleanService.uploadClean(jsonObjectList, apiCode);
                    log.warn("{},apiCode:{},batchNo:{},syncDataClen调用uploadClean结果 code:{},isSuccess:{},msg:{}",
                            TITLE,apiCode,batchNo,callResult.getCode(),callResult.isSuccess(),callResult.getMessage());
                    if (callResult!=null && callResult.isSuccess()) {
                        //调用定制化上传接口
                        List<MarketingPreUserDetailDTO> marketingPreUserDetailDTOS = (List<MarketingPreUserDetailDTO>) callResult.getData();
                        UploadDataDTO uploadDataDTO = initUploadData(apiCode,batchNo, marketingPreUserDetailDTOS);
                        Result<Boolean> pushResult = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                        log.warn("{},apiCode:{},batchNo:{},syncDataClen调用push接口结果:{}", TITLE,apiCode,batchNo,JSONObject.toJSONString(pushResult));
                        if (pushResult != null && pushResult.isSuccess()) {
                            // 修改状态为已清洗
                            tcSyncDataCleanService.updateCleanStatus(idList,1);
                            result = result.success().setDate(tcyrSyncList.size());
                        }else {
                            tcSyncDataCleanService.updateCleanStatus(idList,3);
                            result =  result.failure();
                        }
                    }else {
                        tcSyncDataCleanService.updateCleanStatus(idList,2);
                        result =  result.failure();

                    }
            } catch (Exception e) {
                tcSyncDataCleanService.updateCleanStatus(idList,4);
                log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
                return result.failure();
            }
        }
        log.warn("{},batchNo:{} sycnDataClean成功,successLine:{}",TITLE,batchNo,tcyrSyncList.size());
        return result;
    }

    /**
     * 封装异步调用上传的数据
     *
     * @param apiCode   apiCode
     * @param syncUsers 具体数据对象
     */
    private UploadDataDTO initUploadData(String apiCode,String batchNo, List<MarketingPreUserDetailDTO> syncUsers) {
        String taskId = batchNo;
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        String requestId = apiCode+"_"+taskId+"_"+System.currentTimeMillis()+"_"+randomNumber;
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
