package com.br.marketing.bridge.job.tc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.dto.tc.TcRevokeDto;
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
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
        } catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }

    /**
     * 主方法
     * @param apiCode
     */
    private void action(String apiCode) throws IOException {
        while (true) {
            //1.查询待撤销数据
            MarketingTcyrRevokeRecordExample recordExample = new MarketingTcyrRevokeRecordExample();
            recordExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andStatusEqualTo(1)
                    .andIsCleanEqualTo(0)
                    .andIsDelEqualTo(1);
            recordExample.setOrderByClause("create_time desc limit 1");
            List<MarketingTcyrRevokeRecord> records = marketingTcyrRevokeRecordMapper.selectByExample(recordExample);
            if (CollectionUtils.isEmpty(records)) {
                break;
            }
            //2.组装数据
            MarketingTcyrRevokeRecord record = records.get(0);
            String batchNo = record.getBatchNo();
            TcRevokeDto tcRevokeDto = objectMapper.readValue(record.getData(), TcRevokeDto.class);
            List<String> userKeyList = Optional.ofNullable(tcRevokeDto.getUserKeyList())
                    .filter(list -> !list.isEmpty())
                    .orElseGet(() -> marketingSyncUserMapper.getCustNumsByCusBatchtikv_(apiCode, batchNo));
            List<JSONObject> jsonObjects = userKeyList.stream()
                    .map(userKey -> new JSONObject().fluentPut("userKey", userKey))
                    .collect(Collectors.toList());
            List<List<JSONObject>> partitions = ListUtils.partition(jsonObjects, 1000);
            //3.调用接口
            MarketingTcyrRevokeRecord updateRecord = new MarketingTcyrRevokeRecord();
            updateRecord.setId(record.getId());
            for (List<JSONObject> partition : partitions) {
                try {
                    Result result = generalDataCleanService.transferClean(partition, apiCode, "revoke");
                    if (result != null && result.isSuccess()) {
                        List<TransferDataItemDTO> transferDataItemDTOS = (List<TransferDataItemDTO>) result.getData();
                        PushTransferDataDetailDTO dto = initTransferData(apiCode, transferDataItemDTOS);
                        Result pushResult = pushInfoService.pushTransferByRetry(dto, null);
                        if (pushResult == null || !pushResult.isSuccess()) {
                          log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                                  TITLE + "-数据id：" + record.getId() + "调用pushTransferByRetry方法失败"));
                          updateRecord.setIsClean(3);
                          break;
                        }
                    } else {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                                TITLE + "-数据id：" + record.getId() + "调用transferClean方法失败"));
                        updateRecord.setIsClean(2);
                        break;
                    }
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                            TITLE + "-数据id：" + record.getId() + "清洗+转化出现异常"), e);
                    updateRecord.setIsClean(4);
                    break;
                }
            }
            updateRecord.setIsClean(1);
            marketingTcyrRevokeRecordMapper.updateByPrimaryKeySelective(updateRecord);
        }
    }

    private PushTransferDataDetailDTO initTransferData(String apiCode, List<TransferDataItemDTO> transferDataItems) {
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(transferDataItems);
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String taskId =  yyyyMMdd.concat("_").concat(apiCode);
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        String requestId = apiCode+"_"+taskId+"_"+System.currentTimeMillis()+"_"+randomNumber;
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        return dto;
    }
}
