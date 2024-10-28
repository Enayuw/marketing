package com.br.marketing.service.guomei.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.upload.service.weiju.dto.WeiJuUploadJsonDTO;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.CustomizeUploadData;
import com.br.marketing.mapper.CustomizeUploadDataMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.guomei.GuoMeiDataCleanService;
import com.br.marketing.service.weiju.WeiJuDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GuoMeiDataCleanServiceImpl implements GuoMeiDataCleanService {

    @Resource
    private CustomizeUploadDataMapper customizeUploadDataMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PushInfoService pushInfoService;


    /**
     * 国美前置数据清洗实现
     *
     * @param message 信息
     * @return {@link Result }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/10/28
     */
    @Override
    public Result<Boolean> cleanData(String message) {
        JSONObject jsonObject = JSONObject.parseObject(message);
        String tCid = jsonObject.getString("tCid");
        String sourceId = jsonObject.getString("sourceId");
        CustomizeUploadData data = customizeUploadDataMapper.selectById(tCid, sourceId);
        if (Objects.isNull(data) || StringUtils.isEmpty(data.getRequestJsonData())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.WEIJU_SERVICEERROR.getCode(), "微聚数据清洗，根据id查询待清洗数据为空"));
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        try {
            WeiJuUploadJsonDTO uploadJson = JSON.parseObject(data.getRequestJsonData(), WeiJuUploadJsonDTO.class);
            MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
            userDTO.setTaskId(uploadJson.getExecuteBatchNo());
            userDTO.setRequestId(uploadJson.getTraceId());
            userDTO.setLast("0");
            userDTO.setTotal("0");
            List<MarketingPreUserDetailDTO> dataUploadItems = buildUploadDataItems(uploadJson);
            userDTO.setDataItems(dataUploadItems);
            UploadDataDTO uploadDataDTO = new UploadDataDTO();
            uploadDataDTO.setApiCode(data.getApiCode());
            uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO));
            Result<Boolean> result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);

            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                customizeUploadDataMapper.updateSyncStatusById(tCid, sourceId, 1);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.WEIJU_SERVICEERROR.getCode(), "携程促活，主线程处理异常，前置表id：" + data.getId()), e);
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private List<MarketingPreUserDetailDTO> buildUploadDataItems(WeiJuUploadJsonDTO uploadJson) {
        JSONArray userInfoList = uploadJson.getUserInfoList();
        List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
        JSONObject fieldMapping = marketingCommonConfig.getWeiJuCleanFieldMappingConfig();
        for (int i = 0; i < userInfoList.size(); i++) {
            JSONObject item = userInfoList.getJSONObject(i);
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
            marketingPreUserDetailDTO.setCell(item.getString("mobile"));
            marketingPreUserDetailDTO.setCustNum(item.getString("mobile"));
            JSONObject reserveField1 = new JSONObject();
            reserveField1.put("customNameType", uploadJson.getOperationUserType());
            reserveField1.put("userType", fieldMapping.getString(uploadJson.getScene()));
            reserveField1.put("sleepGroup", JSONArray.parse(uploadJson.getSilenceDaysGroup()));
            if (StringUtils.isNotEmpty(item.getString("registedTime"))) {
                reserveField1.put("registerTime", item.getString("registedTime"));
            }
            if (StringUtils.isNotEmpty(item.getString("lastCreditApplyTime"))) {
                reserveField1.put("auditTime", item.getString("lastCreditApplyTime"));
            }
            if (StringUtils.isNotEmpty(item.getString("lastAllSettleTime"))) {
                reserveField1.put("settleTime", item.getString("lastAllSettleTime"));
            }
            if (StringUtils.isNotEmpty(item.getString("lastcreditAmountRangeLabel"))) {
                reserveField1.put("auditAmountGroup", item.getString("lastcreditAmountRangeLabel"));
            }
            marketingPreUserDetailDTO.setReserveField1(reserveField1.toJSONString());
            dataItems.add(marketingPreUserDetailDTO);
        }
        return dataItems;
    }

}
