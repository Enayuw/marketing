package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.account.*;
import com.br.marketing.entity.LineAccountDetailNormal;
import com.br.marketing.entity.LineAccountLogNormal;
import com.br.marketing.entity.MarketingLineAccountLog;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.OpeTypeEnum;
import com.br.marketing.mapper.LineAccountDetailNormalMapper;
import com.br.marketing.mapper.LineAccountLogNormalMapper;
import com.br.marketing.mapper.LineSupplierInfoNormalMapper;
import com.br.marketing.service.LineSmsAccountDataNormalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class LineSmsAccountDataNormalServiceImpl implements LineSmsAccountDataNormalService {


    @Resource
    private LineAccountDetailNormalMapper lineAccountDetailNormalMapper;

    @Resource
    private LineSupplierInfoNormalMapper lineSupplierInfoNormalMapper;

    @Resource
    private LineAccountLogNormalMapper lineAccountLogNormalMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void addLineAccount(LineAccountDto dto) throws JsonProcessingException{
        long groupId = Long.parseLong(
                ThreadLocalRandom.current().nextInt(100, 1000)
                        + String.valueOf(System.currentTimeMillis()));
        Long lineSupplierId = lineSupplierInfoNormalMapper.selectIdByLineSupplier(dto.getLineSupplier());
        List<Long> gatewayIds = dto.getLines().stream().map(LineCallerDto::getGatewayId).collect(Collectors.toList());
        List<LineAccountDetailNormal> objList = new ArrayList<>();
        gatewayIds.forEach(gatewayId -> {
            for (PriceDateDTO priceDate : dto.getPriceDates()) {
                Date effectStartDate = Date.from(priceDate.getEffectStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date effectEndDate = null;
                if (priceDate.getEffectEndDate() != null) {
                    effectEndDate = Date.from(priceDate.getEffectEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
                LineAccountDetailNormal itemObj = new LineAccountDetailNormal();
                itemObj.setGroupId(groupId);
                itemObj.setLineSupplierId(lineSupplierId);
                itemObj.setGatewayId(gatewayId);
                itemObj.setPrice(priceDate.getPrice());
                itemObj.setEffectStartDate(effectStartDate);
                itemObj.setEffectEndDate(effectEndDate);
                lineAccountDetailNormalMapper.insertSelective(itemObj);
                objList.add(itemObj);
            }
        });

        //TODO 对应日志保存 log->从ThreadContextInfo.getUser() 获取操作用户
        LineAccountLogNormal  logItem = new LineAccountLogNormal();
        logItem.setGroupId(groupId);
        logItem.setLineSupplierId(lineSupplierId);
        JSONObject detail = new JSONObject();
        detail.put("gatewayIds", objectMapper.writeValueAsString(gatewayIds));
        detail.put("priceDates", JSON.toJSONString(dto.getPriceDates()));
        logItem.setDetail(detail.toJSONString());
        userRecord(logItem);
        logItem.setOpeType(OpeTypeEnum.OPE_TYPE_INS.getType());
        lineAccountLogNormalMapper.insertSelective(logItem);
    }

    private void userRecord(LineAccountLogNormal logItem) {
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        if (userDetail != null) {
            if (userDetail.getId() != null) {
                logItem.setUserId(userDetail.getId().toString());
            }
            logItem.setUserName(userDetail.getUserName());
            logItem.setRealName(userDetail.getRealName());
        }
    }
}
