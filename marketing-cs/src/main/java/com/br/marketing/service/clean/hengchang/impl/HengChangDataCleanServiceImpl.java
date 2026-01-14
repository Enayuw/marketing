package com.br.marketing.service.clean.hengchang.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.upload.service.hengchang.dto.HengChangUploadJsonDTO;
import com.br.marketing.aspect.MqIdempotent;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.MqIdempotentContext;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.CustomizeUploadData;
import com.br.marketing.mapper.CustomizeUploadDataMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.hengchang.HengChangDataCleanService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName HengChangDataCleanServiceImpl
 * @Author kongbx
 * @Date 2025/1/4 10:41
 */
@Service
@Slf4j
public class HengChangDataCleanServiceImpl implements HengChangDataCleanService {

    @Resource
    private CustomizeUploadDataMapper customizeUploadDataMapper;

    @Resource
    private PushInfoService pushInfoService;

    @MqIdempotent
    @Override
    public Result<Boolean> cleanData(String message) {
        log.warn("恒昌数据接入：" +message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        String tCid = jsonObject.getString("tCid");
        String sourceId = jsonObject.getString("sourceId");
        CustomizeUploadData data = customizeUploadDataMapper.selectById(tCid, sourceId);
        if (Objects.isNull(data) || StringUtils.isEmpty(data.getRequestJsonData())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HENGCHANG_SERVICEERROR.getCode(), "恒昌数据清洗，根据id查询待清洗数据为空"));
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        try {
            MqIdempotentContext.setApiCode(data.getApiCode());
            HengChangUploadJsonDTO uploadJson = JSON.parseObject(data.getRequestJsonData(), HengChangUploadJsonDTO.class);
            Result<Boolean> result;
            MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
            userDTO.setTaskId(uploadJson.getTaskCode());
            userDTO.setRequestId(data.getRequestId());
            userDTO.setLast("0");
            userDTO.setTotal("0");
            List<MarketingPreUserDetailDTO> dataUploadItems = buildUploadDataItems(uploadJson);
            userDTO.setDataItems(dataUploadItems);
            UploadDataDTO uploadDataDTO = new UploadDataDTO();
            uploadDataDTO.setApiCode(data.getApiCode());
            uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO));
            result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                customizeUploadDataMapper.updateSyncStatusById(tCid, sourceId, 1);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HENGCHANG_SERVICEERROR.getCode(), "恒昌前置数据清洗，主线程处理异常，前置表id：" + data.getId()), e);
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private List<MarketingPreUserDetailDTO> buildUploadDataItems(HengChangUploadJsonDTO uploadJson) {
        JSONArray userInfoList = uploadJson.getUserInfoList();
        List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
        for (int i = 0; i < userInfoList.size(); i++) {
            JSONObject item = userInfoList.getJSONObject(i);
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
            marketingPreUserDetailDTO.setCell(item.getString("phone"));
            marketingPreUserDetailDTO.setName(item.getString("name"));
            marketingPreUserDetailDTO.setCustNum(item.getString("uniqueId"));
            JSONObject reserveField1 = new JSONObject();
            reserveField1.put("userType", uploadJson.getUserType());
            reserveField1.put("batchId", uploadJson.getBatchId());
            reserveField1.put("marketingTime", uploadJson.getMarketingTime());

            if (StringUtils.isNotEmpty(item.getString("registerTime"))) {
                reserveField1.put("registerTime", item.getString("registerTime"));
            }
            if (StringUtils.isNotEmpty(item.getString("lastLoginTime"))) {
                reserveField1.put("loginTime", item.getString("lastLoginTime"));
            }
            if (StringUtils.isNotEmpty(item.getString("creditGrantingTime"))) {
                reserveField1.put("auditTime", item.getString("creditGrantingTime"));
            }
            if (StringUtils.isNotEmpty(item.getString("creditBalance"))) {
                reserveField1.put("unlentAmount", item.getString("creditBalance"));
            }
            if (StringUtils.isNotEmpty(item.getString("settlementTime"))) {
                reserveField1.put("settleTime", item.getString("settlementTime"));
            }
            if (StringUtils.isNotEmpty(item.getString("extra"))) {
                JSONObject jsonObject = JSONObject.parseObject(item.getString("extra"));
                for (String key : jsonObject.keySet()) {
                    Object value = jsonObject.get(key);
                    reserveField1.put(key, value);
                }
            }
            marketingPreUserDetailDTO.setReserveField1(reserveField1.toJSONString());
            dataItems.add(marketingPreUserDetailDTO);
        }
        return dataItems;
    }

}
