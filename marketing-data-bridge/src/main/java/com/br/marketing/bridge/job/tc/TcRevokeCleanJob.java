package com.br.marketing.bridge.job.tc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.dto.tc.TcRevokeDto;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTcyrRevokeRecord;
import com.br.marketing.entity.MarketingTcyrRevokeRecordExample;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.MarketingTcyrRevokeRecordMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @description 同城易融撤销数据清洗任务
 * @author hedongshuo
 * @date 2025/5/7 18:55
 **/
@Component
@Slf4j
public class TcRevokeCleanJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融-撤销数据清洗任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrRevokeRecordMapper marketingTcyrRevokeRecordMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Resource
    private PushInfoService pushInfoService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            action(marketingCommonConfig.getTcyrApiCode());
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
            MarketingTcyrRevokeRecord record = fetchNextRevokeRecord(apiCode);
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
    private MarketingTcyrRevokeRecord fetchNextRevokeRecord(String apiCode) {
        MarketingTcyrRevokeRecordExample example = new MarketingTcyrRevokeRecordExample();
        example.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(1)
                .andIsCleanEqualTo(0)
                .andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc limit 1");
        List<MarketingTcyrRevokeRecord> records = marketingTcyrRevokeRecordMapper.selectByExample(example);
        return CollectionUtils.isEmpty(records) ? null : records.get(0);
    }

    //处理单条记录
    private void processRevokeRecord(String apiCode, MarketingTcyrRevokeRecord record) {
        MarketingTcyrRevokeRecord updateRecord = new MarketingTcyrRevokeRecord();
        updateRecord.setId(record.getId());
        try {
            String batchNo = record.getBatchNo();
            JSONObject recordData = JSONObject.parseObject(record.getData());
            String scene = recordData == null ? null : recordData.getString("scene");
            List<String> userKeyList = Collections.emptyList();
            if (recordData != null) {
                JSONArray arr = recordData.getJSONArray("userKeyList");
                if (arr != null && !arr.isEmpty()) {
                    userKeyList = arr.toJavaList(String.class);
                }
            }
            if (CollectionUtils.isNotEmpty(userKeyList)) {
                processUserKeyList(apiCode, batchNo, scene, userKeyList, updateRecord, record.getId());
            } else {
                processUserKeyListFromDB(apiCode, batchNo, scene, updateRecord, record.getId());
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    TITLE + "-数据id：" + record.getId() + "外层处理撤销记录异常"), e);
            updateRecord.setIsClean(4);
            marketingTcyrRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
        }
    }

    // 处理记录中上送的userKeyList
    private void processUserKeyList(String apiCode, String batchNo, String scene, List<String> userKeyList,
                                    MarketingTcyrRevokeRecord updateRecord, Long recordId) {
        List<List<String>> partitions = ListUtils.partition(userKeyList, 1000);
        if (processPartitions(apiCode, batchNo, scene, partitions, updateRecord, recordId)) {
            updateRecord.setIsClean(1);
            marketingTcyrRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
        }
    }

    // 记录中无userKeyList，从DB中获取并处理
    private void processUserKeyListFromDB(String apiCode, String batchNo, String scene,
                                          MarketingTcyrRevokeRecord updateRecord, Long recordId) {
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
            if (!processPartitions(apiCode, batchNo, scene, partitions, updateRecord, recordId)) {
                return;
            }
        }
        updateRecord.setIsClean(1);
        marketingTcyrRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
    }

    // 抽取方法：处理分区数据
    private boolean processPartitions(String apiCode, String batchNo, String scene, List<List<String>> partitions,
                                   MarketingTcyrRevokeRecord updateRecord, Long recordId) {
        for (List<String> partition : partitions) {
            List<JSONObject> jsonObjects = partition.stream()
                    .map(userKey -> new JSONObject()
                            .fluentPut("userKey", userKey)
                            .fluentPut("batchNo", batchNo)
                            .fluentPut("scene", scene))
                    .collect(Collectors.toList());
            try {
                if (!processTransferClean(apiCode, scene, jsonObjects, updateRecord, recordId)) {
                    return false;
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        TITLE + "-数据id：" + recordId + "处理撤销记录异常"), e);
                updateRecord.setIsClean(4);
                marketingTcyrRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
                return false;
            }
        }
        return true;
    }

    // 清洗+调用转化接口
    private boolean processTransferClean(String apiCode, String scene, List<JSONObject> jsonObjects,
                                         MarketingTcyrRevokeRecord updateRecord, Long recordId) {
        String bizAction = StringUtils.isNotBlank(scene) ? ("revoke-" + scene) : "revoke";
        //清洗
        Result result = generalDataCleanService.transferClean(jsonObjects, apiCode, bizAction);
        if (result == null || !result.isSuccess()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    TITLE + "-数据id：" + recordId + "调用transferClean方法失败,bizAction:" + bizAction));
            updateRecord.setIsClean(2);
            marketingTcyrRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
            return false;
        }
        List<TransferDataItemDTO> transferDataItemDTOS = (List<TransferDataItemDTO>) result.getData();

        //调用转化接口
        PushTransferDataDetailDTO dto = initTransferData(apiCode, transferDataItemDTOS);
        Result pushResult = pushInfoService.pushTransferByRetry(dto, null);
        if (pushResult == null || !pushResult.isSuccess()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    TITLE + "-数据id：" + recordId + "调用pushTransferByRetry方法失败"));
            updateRecord.setIsClean(3);
            marketingTcyrRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
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
