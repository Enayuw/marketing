package com.br.marketing.check.job;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.br.marketing.entity.HaierCollidingDataLog;
import com.br.marketing.mapper.HaierCollidingDataLogMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.mapper.HaierCollidingDataMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.HaierCollidingDataToSyncVO;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HaierCollidingDataSyncJob extends AbstractSimpleElasticJob {

    @Resource
    private HaierCollidingDataLogMapper haierCollidingDataLogMapper;
    @Resource
    private PushInfoService pushInfoService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Integer sendDate = Integer.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<String> apiCodes = marketingCommonConfig.getHaierCollidingDataSyncApiCode();
        apiCodes.forEach((String apiCode) -> {
            for (;;) {
                List<HaierCollidingDataToSyncVO> haierCollidingDatas = haierCollidingDataLogMapper.selectSyncDataList(apiCode, sendDate);
                List<Long> ids = haierCollidingDatas.stream().map(HaierCollidingDataToSyncVO::getId).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(ids)) {
                    break;
                }
                MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
                marketingPreUserDTO.setTaskId(apiCode + "_" + sendDate);
                marketingPreUserDTO.setRequestId(marketingPreUserDTO.getTaskId().concat("_").concat(UUID.randomUUID().toString()));
                List<MarketingPreUserDetailDTO> dataItems = buildMarketingPreUserDetails(haierCollidingDatas);
                marketingPreUserDTO.setDataItems(dataItems);
                UploadDataDTO uploadDataDTO = new UploadDataDTO();
                uploadDataDTO.setApiCode(apiCode);
                uploadDataDTO.setJsonData(JSONObject.toJSONString(marketingPreUserDTO));
                Result result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    haierCollidingDataLogMapper.updateSyncStatusByIds(ids, 2);
                } else {
                    haierCollidingDataLogMapper.updateSyncStatusByIds(ids, 3);
                }
            }
        });
    }

    private static List<MarketingPreUserDetailDTO> buildMarketingPreUserDetails(List<HaierCollidingDataToSyncVO> haierCollidingDatas) {
        List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
        for (HaierCollidingDataToSyncVO data : haierCollidingDatas) {
            MarketingPreUserDetailDTO dto = new MarketingPreUserDetailDTO();
            dto.setCell(data.getMobileDigest());
            dto.setCustNum(data.getMobileDigest());
            JSONObject reserveField1 = new JSONObject();
            reserveField1.put("userType", 1);
            dto.setReserveField1(reserveField1.toJSONString());
            dataItems.add(dto);
        }
        return dataItems;
    }
}
