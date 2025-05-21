package com.br.marketing.bridge.job.tc;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrTransferRecord;
import com.br.marketing.enums.TcTransferRecordStatusEnum;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.tc.TcTransferRecordService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 同城易融转化数据清洗任务
 * @Author zhiyong.zhang
 * @CreateTime 2025/04/21
 */
@Component
@Slf4j
public class TcTransferCleanJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融-转化数据清洗任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcTransferRecordService tcTransferRecordService;


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
     * 转化上传操作
     * @param apiCode
     */
    private void atciton(String apiCode) {
        Long lastSearchId = 0L;
        Integer searchSize =  marketingCommonConfig.getTcPageSearchSize();
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(10, 10);
        List<CompletableFuture<Result>> futureList = new ArrayList<>();
        List<Long> resultList = new ArrayList<>(20);

        while (true) {
            List<MarketingTcyrTransferRecord> tcyrTransferRecordList = tcTransferRecordService.selectTcyrTransforRecordList(apiCode,
                    TcTransferRecordStatusEnum.ACCESS_SUCCESS.getValue(),lastSearchId,searchSize);
            if (CollectionUtils.isEmpty(tcyrTransferRecordList)) {
                break;
            }
            processList(apiCode,tcyrTransferRecordList,actionPool,futureList,resultList);
            lastSearchId = tcyrTransferRecordList.get(tcyrTransferRecordList.size()-1).getId();
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        Long successLine = 0L;
        for (Long successCount : resultList) {
            successLine += successCount;
        }
        log.warn("{},apiCode:{}, transferClean process complete,successLine:{}",TITLE,apiCode,successLine);
    }

    private Result processList(String apiCode,List<MarketingTcyrTransferRecord> tcyrTransferRecordList,
                               ThreadPoolExecutor actionPool, List<CompletableFuture<Result>> futureList, List<Long> resultList) {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(tcyrTransferRecordList)) {
            return result.success();
        }
        actionPool.setCorePoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        actionPool.setMaximumPoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        futureList.add(CompletableFuture.supplyAsync(() -> processData(apiCode,tcyrTransferRecordList), actionPool)
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

    private Result processData(String apiCode,List<MarketingTcyrTransferRecord> tcyrTransferRecordList) {
        Result result = new Result().failure();
        List<List<MarketingTcyrTransferRecord>> partitionList = ListUtils.partition(tcyrTransferRecordList, 1000);
        for (List<MarketingTcyrTransferRecord> tcyrSyncItemList : partitionList) {
            List<Long> idList = tcyrSyncItemList.stream().map(MarketingTcyrTransferRecord::getId).collect(Collectors.toList());
            try {
                List<JSONObject> jsonObjectList = tcyrSyncItemList.stream().map(
                        m->JSONObject.parseObject(m.getData())).collect(Collectors.toList());
                Result transferResult = generalDataCleanService.transferClean(jsonObjectList,apiCode);
                log.warn("{},调用transfer方法 code:{},isSuccess:{},msg:{}",TITLE,transferResult.getCode(),transferResult.isSuccess(),transferResult.getMessage());
                if (transferResult !=null && transferResult.isSuccess()) {
                    List<TransferDataItemDTO> transferDataItemDTOS = (List<TransferDataItemDTO>) transferResult.getData();
                    if (CollectionUtils.isEmpty(transferDataItemDTOS)) {
                        return result.success();
                    }
                    PushTransferDataDetailDTO dto = initTransferData(apiCode,transferDataItemDTOS);
                    Result pushResult = pushInfoService.pushTransferByRetry(dto, null);
                    log.warn("{},调用push接口 code:{},isSuccess:{},msg:{}",TITLE,pushResult.getCode(),pushResult.isSuccess(),pushResult.getMessage());
                    if (pushResult!=null && pushResult.isSuccess()) {
                        tcTransferRecordService.updateCleanStatus(idList,1);
                    }else {
                        tcTransferRecordService.updateCleanStatus(idList,3);
                        return result.failure();
                    }
                }else {
                    tcTransferRecordService.updateCleanStatus(idList,2);
                    return result.failure();
                }
            }catch (Exception e) {
                tcTransferRecordService.updateCleanStatus(idList,4);
                log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
                return result.failure();
            }
        }
        return result.success().setDate(tcyrTransferRecordList.size());
    }

    private PushTransferDataDetailDTO initTransferData(String apiCode, List<TransferDataItemDTO> transferDataItems) {
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(transferDataItems);
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        String requestId = apiCode+"_"+System.currentTimeMillis()+"_"+randomNumber;
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        return dto;
    }

}
