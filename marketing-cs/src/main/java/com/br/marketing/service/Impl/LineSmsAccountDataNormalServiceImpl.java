package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.account.*;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.OpeTypeEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.LineSmsAccountDataNormalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
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

    //禁用
    private static final Integer ENABLED_FORB = 0;

    //启用
    private static final Integer ENABLED_ACT = 1;

    //删除
    private static final Integer ISDELETED_DEL = 1;


    @Resource
    private LineAccountDetailNormalMapper lineAccountDetailNormalMapper;

    @Resource
    private LineSupplierInfoNormalMapper lineSupplierInfoNormalMapper;

    @Resource
    private LineAccountLogNormalMapper lineAccountLogNormalMapper;

    @Resource
    private SmsAccountDetailNormalMapper smsAccountDetailNormalMapper;

    @Resource
    private SmsAccountLogNormalMapper smsAccountLogNormalMapper;

    @Resource
    private SmsVendorInfoNormalMapper smsVendorInfoNormalMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void addLineAccount(LineAccountDto dto) throws JsonProcessingException{
        long groupId = Long.parseLong(
                ThreadLocalRandom.current().nextInt(100, 1000)
                        + String.valueOf(System.currentTimeMillis()));
        Long lineSupplierId = lineSupplierInfoNormalMapper.selectIdByLineSupplierNoOpeStatus(dto.getLineSupplier());
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

        //对应日志保存 log->从ThreadContextInfo.getUser() 获取操作用户
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


    @Override
    public void updLineAccount(LineAccountDto dto) throws JsonProcessingException{
        //1.删除detail
        LineAccountDetailNormalExample lineAccountDetailNormalExample = new LineAccountDetailNormalExample();
        lineAccountDetailNormalExample.createCriteria().andGroupIdEqualTo(dto.getGroupId());
        LineAccountDetailNormal updateAccountDetailNormal = new LineAccountDetailNormal();
        updateAccountDetailNormal.setIsDelete(ISDELETED_DEL);
        lineAccountDetailNormalMapper.updateByExampleSelective(updateAccountDetailNormal, lineAccountDetailNormalExample);

        //2.新增
        Long lineSupplierId = lineSupplierInfoNormalMapper.selectIdByLineSupplierNoOpeStatus(dto.getLineSupplier());
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
                itemObj.setGroupId(dto.getGroupId());
                itemObj.setLineSupplierId(lineSupplierId);
                itemObj.setGatewayId(gatewayId);
                itemObj.setPrice(priceDate.getPrice());
                itemObj.setEffectStartDate(effectStartDate);
                itemObj.setEffectEndDate(effectEndDate);
                lineAccountDetailNormalMapper.insertSelective(itemObj);
                objList.add(itemObj);
            }
        });

        //对应日志保存 log->从ThreadContextInfo.getUser() 获取操作用户
        LineAccountLogNormal  logItem = new LineAccountLogNormal();
        logItem.setGroupId(dto.getGroupId());
        logItem.setLineSupplierId(lineSupplierId);
        JSONObject detail = new JSONObject();
        detail.put("gatewayIds", objectMapper.writeValueAsString(gatewayIds));
        detail.put("priceDates", JSON.toJSONString(dto.getPriceDates()));
        logItem.setDetail(detail.toJSONString());
        userRecord(logItem);
        logItem.setOpeType(OpeTypeEnum.OPE_TYPE_UPD.getType());
        lineAccountLogNormalMapper.insertSelective(logItem);
    }


    @Override
    @Transactional
    public void forbLineAccount(Long groupId) {
        //2.禁用detail
        LineAccountDetailNormalExample accountDetailNormalExample = new LineAccountDetailNormalExample();
        accountDetailNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        LineAccountDetailNormal updateAccountDetailNormal = new LineAccountDetailNormal();
        updateAccountDetailNormal.setEnabled(ENABLED_FORB);
        lineAccountDetailNormalMapper.updateByExampleSelective(updateAccountDetailNormal, accountDetailNormalExample);

        //3.新增禁用日志
        LineAccountLogNormalExample logNormalExample = new LineAccountLogNormalExample();
        logNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        logNormalExample.setOrderByClause("create_time desc limit 1");
        LineAccountLogNormal oldLogNormal = lineAccountLogNormalMapper.selectByExample(logNormalExample).get(0);
        LineAccountLogNormal newLogNormal = new LineAccountLogNormal();
        BeanUtils.copyProperties(oldLogNormal, newLogNormal);
        newLogNormal.setId(null);
        userRecord(newLogNormal);
        newLogNormal.setOpeType(OpeTypeEnum.OPE_TYPE_FOB.getType());
        newLogNormal.setCreateTime(null);
        newLogNormal.setUpdateTime(null);
        lineAccountLogNormalMapper.insertSelective(newLogNormal);
    }

    @Override
    @Transactional
    public void allowLineAccount(Long groupId) {
        //2.启用detail
        LineAccountDetailNormalExample accountDetailNormalExample = new LineAccountDetailNormalExample();
        accountDetailNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        LineAccountDetailNormal updateAccountDetailNormal = new LineAccountDetailNormal();
        updateAccountDetailNormal.setEnabled(ENABLED_ACT);
        lineAccountDetailNormalMapper.updateByExampleSelective(updateAccountDetailNormal, accountDetailNormalExample);
        //3.新增启用日志
        LineAccountLogNormalExample logNormalExample = new LineAccountLogNormalExample();
        logNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        logNormalExample.setOrderByClause("create_time desc limit 1");
        LineAccountLogNormal oldLogNormal = lineAccountLogNormalMapper.selectByExample(logNormalExample).get(0);
        LineAccountLogNormal newLogNormal = new LineAccountLogNormal();
        BeanUtils.copyProperties(oldLogNormal, newLogNormal);
        newLogNormal.setId(null);
        userRecord(newLogNormal);
        newLogNormal.setOpeType(OpeTypeEnum.OPE_TYPE_ALLOW.getType());
        newLogNormal.setCreateTime(null);
        newLogNormal.setUpdateTime(null);
        lineAccountLogNormalMapper.insertSelective(newLogNormal);
    }

    @Override
    public void deleteLineAccount(Long groupId) {
        //2.删除配置(id_delete=1)
        LineAccountDetailNormalExample lineAccountDetailNormalExample = new LineAccountDetailNormalExample();
        lineAccountDetailNormalExample.createCriteria().andGroupIdEqualTo(groupId);
        LineAccountDetailNormal updateAccountDetailNormal = new LineAccountDetailNormal();
        updateAccountDetailNormal.setIsDelete(ISDELETED_DEL);
        lineAccountDetailNormalMapper.updateByExampleSelective(updateAccountDetailNormal, lineAccountDetailNormalExample);

        //3.新增删除日志
        LineAccountLogNormalExample logNormalExample = new LineAccountLogNormalExample();
        logNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        logNormalExample.setOrderByClause("create_time desc limit 1");
        LineAccountLogNormal oldLogNormal = lineAccountLogNormalMapper.selectByExample(logNormalExample).get(0);
        LineAccountLogNormal newLogNormal = new LineAccountLogNormal();
        BeanUtils.copyProperties(oldLogNormal, newLogNormal);
        newLogNormal.setId(null);
        userRecord(newLogNormal);
        newLogNormal.setOpeType(OpeTypeEnum.OPE_TYPE_DEL.getType());
        newLogNormal.setCreateTime(null);
        newLogNormal.setUpdateTime(null);
        lineAccountLogNormalMapper.insertSelective(newLogNormal);
    }

    @Override
    public void addSmsAccount(SmsAccountDto dto)  throws JsonProcessingException{
        long groupId = Long.parseLong(
                ThreadLocalRandom.current().nextInt(100, 1000)
                        + String.valueOf(System.currentTimeMillis()));
        List<Long> channelIds = dto.getChannels().stream().map(SmsChannelDto::getChannelId).collect(Collectors.toList());
        channelIds.forEach(channelId -> {
            for (PriceDateDTO priceDate : dto.getPriceDates()) {
                Date effectStartDate = Date.from(priceDate.getEffectStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date effectEndDate = null;
                if (priceDate.getEffectEndDate() != null) {
                    effectEndDate = Date.from(priceDate.getEffectEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
                SmsAccountDetailNormal itemObj = new SmsAccountDetailNormal();
                itemObj.setGroupId(groupId);
                itemObj.setVendorId(dto.getVendorId());
                itemObj.setChannelId(channelId);
                itemObj.setPrice(priceDate.getPrice());
                itemObj.setEffectStartDate(effectStartDate);
                itemObj.setEffectStartDate(effectStartDate);
                itemObj.setEffectEndDate(effectEndDate);
                smsAccountDetailNormalMapper.insertSelective(itemObj);
            }
        });

        //对应日志保存 log->从ThreadContextInfo.getUser() 获取操作用户
        SmsAccountLogNormal  logItem = new SmsAccountLogNormal();
        logItem.setGroupId(groupId);
        logItem.setVendorId(dto.getVendorId());
        JSONObject detail = new JSONObject();
        detail.put("channelIds", objectMapper.writeValueAsString(channelIds));
        detail.put("priceDates", JSON.toJSONString(dto.getPriceDates()));
        logItem.setDetail(detail.toJSONString());
        userRecord(logItem);
        logItem.setOpeType(OpeTypeEnum.OPE_TYPE_INS.getType());
        smsAccountLogNormalMapper.insertSelective(logItem);
    }

    @Override
    public void updSmsAccount(SmsAccountDto dto) throws JsonProcessingException {
        //1.删除detail
        SmsAccountDetailNormalExample smsDetailExample = new SmsAccountDetailNormalExample();
        smsDetailExample.createCriteria().andGroupIdEqualTo(dto.getGroupId());
        SmsAccountDetailNormal updateAccountDetailNormal = new SmsAccountDetailNormal();
        updateAccountDetailNormal.setIsDelete(ISDELETED_DEL);
        smsAccountDetailNormalMapper.updateByExampleSelective(updateAccountDetailNormal, smsDetailExample);

        //2.新增
        List<Long> channelIds = dto.getChannels().stream().map(SmsChannelDto::getChannelId).collect(Collectors.toList());
        channelIds.forEach(channelId -> {
            for (PriceDateDTO priceDate : dto.getPriceDates()) {
                Date effectStartDate = Date.from(priceDate.getEffectStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date effectEndDate = null;
                if (priceDate.getEffectEndDate() != null) {
                    effectEndDate = Date.from(priceDate.getEffectEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
                SmsAccountDetailNormal itemObj = new SmsAccountDetailNormal();
                itemObj.setGroupId(dto.getGroupId());
                itemObj.setVendorId(dto.getVendorId());
                itemObj.setChannelId(channelId);
                itemObj.setPrice(priceDate.getPrice());
                itemObj.setEffectStartDate(effectStartDate);
                itemObj.setEffectStartDate(effectStartDate);
                itemObj.setEffectEndDate(effectEndDate);
                smsAccountDetailNormalMapper.insertSelective(itemObj);
            }
        });

        //对应日志保存 log->从ThreadContextInfo.getUser() 获取操作用户
        SmsAccountLogNormal  logItem = new SmsAccountLogNormal();
        logItem.setGroupId(dto.getGroupId());
        logItem.setVendorId(dto.getVendorId());
        JSONObject detail = new JSONObject();
        detail.put("channelIds", objectMapper.writeValueAsString(channelIds));
        detail.put("priceDates", JSON.toJSONString(dto.getPriceDates()));
        logItem.setDetail(detail.toJSONString());
        userRecord(logItem);
        logItem.setOpeType(OpeTypeEnum.OPE_TYPE_UPD.getType());
        smsAccountLogNormalMapper.insertSelective(logItem);
    }

    @Override
    public void allowSmsAccount(Long groupId) {
        //2.启用detail
        SmsAccountDetailNormalExample accountDetailNormalExample = new SmsAccountDetailNormalExample();
        accountDetailNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        SmsAccountDetailNormal updateAccountDetailNormal = new SmsAccountDetailNormal();
        updateAccountDetailNormal.setEnabled(ENABLED_ACT);
        smsAccountDetailNormalMapper.updateByExampleSelective(updateAccountDetailNormal, accountDetailNormalExample);
        //3.新增启用日志
        SmsAccountLogNormalExample logNormalExample = new SmsAccountLogNormalExample();
        logNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        logNormalExample.setOrderByClause("create_time desc limit 1");
        SmsAccountLogNormal oldLogNormal = smsAccountLogNormalMapper.selectByExample(logNormalExample).get(0);
        SmsAccountLogNormal newLogNormal = new SmsAccountLogNormal();
        BeanUtils.copyProperties(oldLogNormal, newLogNormal);
        newLogNormal.setId(null);
        userRecord(newLogNormal);
        newLogNormal.setOpeType(OpeTypeEnum.OPE_TYPE_ALLOW.getType());
        newLogNormal.setCreateTime(null);
        newLogNormal.setUpdateTime(null);
        smsAccountLogNormalMapper.insertSelective(newLogNormal);
    }

    @Override
    public void forbSmsAccount(Long groupId) {
        //2.禁用detail
        SmsAccountDetailNormalExample accountDetailNormalExample = new SmsAccountDetailNormalExample();
        accountDetailNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        SmsAccountDetailNormal updateAccountDetailNormal = new SmsAccountDetailNormal();
        updateAccountDetailNormal.setEnabled(ENABLED_FORB);
        smsAccountDetailNormalMapper.updateByExampleSelective(updateAccountDetailNormal, accountDetailNormalExample);

        //3.新增禁用日志
        SmsAccountLogNormalExample logNormalExample = new SmsAccountLogNormalExample();
        logNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        logNormalExample.setOrderByClause("create_time desc limit 1");
        SmsAccountLogNormal oldLogNormal = smsAccountLogNormalMapper.selectByExample(logNormalExample).get(0);
        SmsAccountLogNormal newLogNormal = new SmsAccountLogNormal();
        BeanUtils.copyProperties(oldLogNormal, newLogNormal);
        newLogNormal.setId(null);
        userRecord(newLogNormal);
        newLogNormal.setOpeType(OpeTypeEnum.OPE_TYPE_FOB.getType());
        newLogNormal.setCreateTime(null);
        newLogNormal.setUpdateTime(null);
        smsAccountLogNormalMapper.insertSelective(newLogNormal);
    }

    @Override
    public void deleteSmsAccount(Long groupId) {
        //2.删除配置(id_delete=1)
        SmsAccountDetailNormalExample smsAccountDetailNormalExample = new SmsAccountDetailNormalExample();
        smsAccountDetailNormalExample.createCriteria().andGroupIdEqualTo(groupId);
        SmsAccountDetailNormal updateAccountDetailNormal = new SmsAccountDetailNormal();
        updateAccountDetailNormal.setIsDelete(ISDELETED_DEL);
        smsAccountDetailNormalMapper.updateByExampleSelective(updateAccountDetailNormal, smsAccountDetailNormalExample);

        //3.新增删除日志
        SmsAccountLogNormalExample logNormalExample = new SmsAccountLogNormalExample();
        logNormalExample.createCriteria().andGroupIdEqualTo(groupId).andIsDeleteEqualTo(0);
        logNormalExample.setOrderByClause("create_time desc limit 1");
        SmsAccountLogNormal oldLogNormal = smsAccountLogNormalMapper.selectByExample(logNormalExample).get(0);
        SmsAccountLogNormal newLogNormal = new SmsAccountLogNormal();
        BeanUtils.copyProperties(oldLogNormal, newLogNormal);
        newLogNormal.setId(null);
        userRecord(newLogNormal);
        newLogNormal.setOpeType(OpeTypeEnum.OPE_TYPE_DEL.getType());
        newLogNormal.setCreateTime(null);
        newLogNormal.setUpdateTime(null);
        smsAccountLogNormalMapper.insertSelective(newLogNormal);
    }

    private void userRecord(SmsAccountLogNormal logItem) {
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        if (userDetail != null) {
            if (userDetail.getId() != null) {
                logItem.setUserId(userDetail.getId().toString());
            }
            logItem.setUserName(userDetail.getUserName());
            logItem.setRealName(userDetail.getRealName());
        }
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
