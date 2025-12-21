package com.br.marketing.monkey.service.didi;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.DiDiSmsRequestTO;
import com.br.marketing.client.didi.output.v5.DiDiV5CollidingResultResponseDTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.DidiCallBackData;
import com.br.marketing.entity.DidiCallBackDataExample;
import com.br.marketing.entity.DidiCallbackDataLog;
import com.br.marketing.enums.CallBackPushStatusEnum;
import com.br.marketing.mapper.DidiCallBackDataMapper;
import com.br.marketing.mapper.DidiCallbackDataLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DidiCallbackDataLogServiceImpl implements DidiCallbackDataLogService {

    @Resource
    private DiDiV5Client diDiV5Client;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DidiCallBackDataMapper didiCallBackDataMapper;

    @Resource
    private DidiCallbackDataLogMapper didiCallbackDataLogMapper;

    @Override
    public void process() {
        DidiCallBackDataExample dataExample = new DidiCallBackDataExample();
        DidiCallBackDataExample.Criteria dataCriteria = dataExample.createCriteria();
        dataCriteria.andCreateTimeGreaterThan(new Date())
                .andPushStatusEqualTo(CallBackPushStatusEnum.TOBEEXECUTED.getValue());
        if (didiCallBackDataMapper.countByExample(dataExample) == 0) {
            return;
        }
        JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
        int limit = collidingConfig.getInteger("limit") != null ? collidingConfig.getInteger("limit") : 2000;
        String mediaName = collidingConfig.getString("mediaName") != null ? collidingConfig.getString("mediaName") : "bairongC";
        String token = collidingConfig.getString("token") != null ? collidingConfig.getString("token") : "9Hqeoi36CJfdA7n4";

        dataCriteria.andIsConnectEqualTo(1);
        List<DidiCallBackData> callBackDataList = didiCallBackDataMapper.selectByExample(dataExample);

        if (CollectionUtils.isNotEmpty(callBackDataList)) {
            Map<String, List<DidiCallBackData>> groupedByCell = callBackDataList.stream()
                    .collect(Collectors.groupingBy(DidiCallBackData::getCell));

            groupedByCell.forEach((cell, dataList) -> {
                if (dataList.size() == 1) {
                    DidiCallBackData firstData = dataList.get(0);
                    firstData.setStatus(0);
                    push(firstData, mediaName, token);
                } else {
                    DidiCallBackData firstData = dataList.get(0);
                    firstData.setStatus(0);
                    push(firstData, mediaName, token);
                    for (int i = 1; i < dataList.size(); i++) {
                        dataList.get(i).setStatus(1);
                    }
                }
            });
        }
    }

    private String push(DidiCallBackData data, String mediaName, String token) {
        DiDiSmsRequestTO requestTO = new DiDiSmsRequestTO();
        String timestamp = String.valueOf(System.currentTimeMillis());
        requestTO.setSign(data.getCell());
        requestTO.setTimestamp(timestamp);
        requestTO.setSignature(MD5Util.encode(data.getCell() + timestamp + token));
        Result<String> response = diDiV5Client.callbackSuccess(mediaName, requestTO);
        String returnContent = response.getData();
        JSONObject jsonObject = JSONObject.parseObject(returnContent);
        String httpcode = jsonObject.getString("httpcode");
        String content = jsonObject.getString("content");
        DidiCallbackDataLog dataLog = new DidiCallbackDataLog();
        BeanUtils.copyProperties(data, dataLog);
        dataLog.setCallbackId(data.getId());
        if ("200".equals(httpcode) || StringUtils.isNotBlank(content)) {
            DiDiV5CollidingResultResponseDTO diDiV5CollidingResultResponseDTO = JSON.parseObject(content, DiDiV5CollidingResultResponseDTO.class);
            dataLog.setErrorCode(diDiV5CollidingResultResponseDTO.getErrorCode());
            dataLog.setErrorMessage(diDiV5CollidingResultResponseDTO.getErrorMessage());
            didiCallbackDataLogMapper.insertSelective(dataLog);
            data.setPushStatus(2);
        } else {
            data.setPushStatus(3);
        }
        didiCallbackDataLogMapper.insertSelective(dataLog);
        return null;
    }
}
