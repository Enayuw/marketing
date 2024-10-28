package com.br.marketing.service.guomei.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.upload.service.guomei.dto.GuMeUploadJsonDTO;
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
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.GUOMEI_SERVICEERROR.getCode(), "微聚数据清洗，根据id查询待清洗数据为空"));
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        try {
            GuMeUploadJsonDTO uploadJson = JSON.parseObject(data.getRequestJsonData(), GuMeUploadJsonDTO.class);
            MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
            userDTO.setTaskId(String.valueOf(uploadJson.getBatch()));
            userDTO.setRequestId(uploadJson.getRequestId());
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
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.GUOMEI_SERVICEERROR.getCode(), "携程促活，主线程处理异常，前置表id：" + data.getId()), e);
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private List<MarketingPreUserDetailDTO> buildUploadDataItems(GuMeUploadJsonDTO uploadJson) {
        JSONArray userList = uploadJson.getUserList();
        List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
        JSONObject fieldMapping = marketingCommonConfig.getGuoMeiCleanFieldMappingConfig();
        for (int i = 0; i < userList.size(); i++) {
            JSONObject item = userList.getJSONObject(i);
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
            marketingPreUserDetailDTO.setCell(item.getString("cell"));
            marketingPreUserDetailDTO.setCustNum(item.getString("userId"));
            JSONObject reserveField1 = new JSONObject();
            reserveField1.put("firstName", StringUtils.isNotEmpty(item.getString("firstName")) ? item.getString("firstName") : "");

            reserveField1.put("userType", fieldMapping.getString(item.getString("userType")));

            reserveField1.put("gender", fieldMapping.getString(item.getString("gender")));

            reserveField1.put("registerTime", StringUtils.isNotEmpty(item.getString("registerTime")) ?
                    DateUtil.formatDate(DateUtil.parse(item.getString("registerTime"), DatePattern.NORM_DATETIME_FORMAT)) : "");

            reserveField1.put("auditTime", StringUtils.isNotEmpty(item.getString("audittTime")) ?
                    DateUtil.formatDate(DateUtil.parse(item.getString("audittTime"), DatePattern.NORM_DATE_FORMAT)) : "");

            reserveField1.put("auditAmount", StringUtils.isNotEmpty(item.getString("auditAmount")) ? item.getString("auditAmount") : "");

            reserveField1.put("lentTime", StringUtils.isNotEmpty(item.getString("lastloan")) ?
                    DateUtil.formatDate(DateUtil.parse(item.getString("lastloan"), DatePattern.NORM_DATE_FORMAT)) : "");

            reserveField1.put("lentAmount", StringUtils.isNotEmpty(item.getString("lastamount")) ? item.getString("lastamount") : "");

            reserveField1.put("settleTime", StringUtils.isNotEmpty(item.getString("lastsettle")) ?
                    DateUtil.formatDate(DateUtil.parse(item.getString("lastsettle"), DatePattern.NORM_DATETIME_FORMAT)) : "");

            reserveField1.put("age", StringUtils.isNotEmpty(item.getString("age")) ? item.getString("age") : "");

            reserveField1.put("lastboot", StringUtils.isNotEmpty(item.getString("lastboot")) ? item.getString("lastboot") : "");

            reserveField1.put("planId", uploadJson.getPlanId() != null ? String.valueOf(uploadJson.getPlanId()) : "");

            reserveField1.put("customNameType", StringUtils.isNotEmpty(item.getString("customName")) ? item.getString("customName") : "");

            marketingPreUserDetailDTO.setReserveField1(reserveField1.toJSONString());
            dataItems.add(marketingPreUserDetailDTO);
        }
        return dataItems;
    }

}
