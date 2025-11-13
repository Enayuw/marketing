package com.br.marketing.bridge.job.tccpa;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.dto.tc.TcRevokeDto;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaIsDelEnum;
import com.br.marketing.enums.TcCpaRecordStatusEnum;
import com.br.marketing.enums.TcRecordCleanStatusEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.MarketingTcyrCpaRevokeRecordMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @description 同城易融CPA-撤销数据清洗任务
 * @author hedongshuo
 * @date 2025/8/12 9:21
 **/
@Component
@Slf4j
public class TcCpaRevokeCleanJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融cpa-撤销数据清洗任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrCpaRevokeRecordMapper marketingTcyrCpaRevokeRecordMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Resource
    private PushInfoService pushInfoService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            action(marketingCommonConfig.getTcyrCpaApiCode());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }

    /**
     * 主方法
     * @param apiCode
     */
    private void action(String apiCode) {
        while (true) {
            // 1.查询待撤销数据
            MarketingTcyrCpaRevokeRecord record = fetchNextRevokeRecord(apiCode);
            if (record == null) {
                break;
            }
            // 2.处理记录
            processRevokeRecord(apiCode, record);
            // 适当休眠避免CPU过载
            sleepSafely(1000);
        }
    }

    /**
     * @description 查询单条撤销记录
     * @param apiCode
     * @return
     * @author hedongshuo
     * @date 2025/5/8 14:45
     **/
    private MarketingTcyrCpaRevokeRecord fetchNextRevokeRecord(String apiCode) {
        MarketingTcyrCpaRevokeRecordExample example = new MarketingTcyrCpaRevokeRecordExample();
        example.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(TcCpaRecordStatusEnum.ACCESS_SUCCESS.getValue())
                .andIsCleanEqualTo(TcRecordCleanStatusEnum.CLEAN_WAITED.getValue())
                .andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue());
        example.setOrderByClause("create_time desc limit 1");
        List<MarketingTcyrCpaRevokeRecord> records = marketingTcyrCpaRevokeRecordMapper.selectByExample(example);
        return CollectionUtils.isEmpty(records) ? null : records.get(0);
    }

    //处理单条记录
    private void processRevokeRecord(String apiCode, MarketingTcyrCpaRevokeRecord record) {
        MarketingTcyrCpaRevokeRecord updateRecord = new MarketingTcyrCpaRevokeRecord();
        updateRecord.setId(record.getId());
        try {
            String batchNo = record.getBatchNo();
            TcRevokeDto tcRevokeDto = objectMapper.readValue(record.getData(), TcRevokeDto.class);
            if (CollectionUtils.isNotEmpty(tcRevokeDto.getUserKeyList())) {
                processUserKeyList(apiCode, batchNo, tcRevokeDto.getUserKeyList(), updateRecord, record.getId());
            } else {
                processUserKeyListFromDB(apiCode, batchNo, updateRecord, record.getId());
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    TITLE + "-数据id：" + record.getId() + "外层处理撤销记录异常"), e);
            updateRecord.setIsClean(TcRecordCleanStatusEnum.CLEAN_EXCEPTION.getValue());
            marketingTcyrCpaRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
        }
    }

    // 处理记录中上送的userKeyList
    private void processUserKeyList(String apiCode, String batchNo, List<String> userKeyList,
                                    MarketingTcyrCpaRevokeRecord updateRecord, Long recordId) {
        List<List<String>> partitions = ListUtils.partition(userKeyList, 1000);
        if (processPartitions(apiCode, batchNo, partitions, updateRecord, recordId)) {
            updateRecord.setIsClean(TcRecordCleanStatusEnum.CLEAN_COMPLETED.getValue());
            marketingTcyrCpaRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
        }
    }

    // 记录中无userKeyList，从DB中获取并处理
    private void processUserKeyListFromDB(String apiCode, String batchNo,
                                          MarketingTcyrCpaRevokeRecord updateRecord, Long recordId) {
        Long minId = null;
        Integer pageSize = marketingCommonConfig.getTcRevokePageSize();
        while (true) {
            List<MarketingSyncUser> syncUsers = marketingSyncUserMapper.getCustNumsByCusBatchtikv_(
                    apiCode, batchNo, minId, pageSize);
            if (CollectionUtils.isEmpty(syncUsers)) {
                break;
            }
            minId = syncUsers.get(syncUsers.size() - 1).getId();
            List<String> userKeyList = syncUsers.stream()
                    .map(MarketingSyncUser::getCustNum)
                    .distinct()
                    .collect(Collectors.toList());
            List<List<String>> partitions = ListUtils.partition(userKeyList, 1000);
            if (!processPartitions(apiCode, batchNo, partitions, updateRecord, recordId)) {
                return;
            }
        }
        updateRecord.setIsClean(TcRecordCleanStatusEnum.CLEAN_COMPLETED.getValue());
        marketingTcyrCpaRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
    }

    // 抽取方法：处理分区数据
    private boolean processPartitions(String apiCode, String batchNo, List<List<String>> partitions,
                                      MarketingTcyrCpaRevokeRecord updateRecord, Long recordId) {
        for (List<String> partition : partitions) {
            List<JSONObject> jsonObjects = partition.stream()
                    .map(userKey -> new JSONObject()
                            .fluentPut("userKey", userKey)
                            .fluentPut("batchNo", batchNo)
                            .fluentPut("recordId", updateRecord.getId().toString()))
                    .collect(Collectors.toList());
            try {
                if (!processTransferClean(apiCode, jsonObjects, updateRecord, recordId)) {
                    return false;
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        TITLE + "-数据id：" + recordId + "处理撤销记录异常"), e);
                updateRecord.setIsClean(TcRecordCleanStatusEnum.CLEAN_EXCEPTION.getValue());
                marketingTcyrCpaRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
                return false;
            }
        }
        return true;
    }

    // 清洗+调用转化接口
    private boolean processTransferClean(String apiCode, List<JSONObject> jsonObjects,
                                         MarketingTcyrCpaRevokeRecord updateRecord, Long recordId) {
        //清洗
        Result result = generalDataCleanService.transferClean(jsonObjects, apiCode, "revoke");
        if (result == null || !result.isSuccess()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    TITLE + "-数据id：" + recordId + "调用transferClean方法失败"));
            updateRecord.setIsClean(TcRecordCleanStatusEnum.CLEAN_CLEAN_FAIL.getValue());
            marketingTcyrCpaRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
            return false;
        }
        List<TransferDataItemDTO> transferDataItemDTOS = (List<TransferDataItemDTO>) result.getData();

        //调用转化接口
        PushTransferDataDetailDTO dto = initTransferData(apiCode, transferDataItemDTOS);
        Result pushResult = pushInfoService.pushTransferByRetry(dto, null);
        if (pushResult == null || !pushResult.isSuccess()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    TITLE + "-数据id：" + recordId + "调用pushTransferByRetry方法失败"));
            updateRecord.setIsClean(TcRecordCleanStatusEnum.CLEAN_PUSH.getValue());
            marketingTcyrCpaRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
            return false;
        }
        return true;
    }

    // 安全休眠方法
    private void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("线程休眠被中断");
        }
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
