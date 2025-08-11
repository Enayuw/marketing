package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.account.*;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.OpeTypeEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.LineSmsAccountDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class LineSmsAccountDataServiceImpl implements LineSmsAccountDataService {

    @Resource
    MarketingSmsAccountDetailMapper smsAccountDetailMapper;

    @Resource
    MarketingSmsAccountRecordMapper smsAccountRecordMapper;

    @Resource
    MarketingSmsAccountLogMapper smsAccountLogMapper;

    @Resource
    MarketingLineAccountDetailMapper lineAccountDetailMapper;

    @Resource
    MarketingLineAccountRecordMapper lineAccountRecordMapper;

    @Resource
    MarketingLineAccountLogMapper lineAccountLogMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    //禁用
    private static final Integer ENABLED_FORB = 0;

    //启用
    private static final Integer ENABLED_ACT = 1;

    //正常
    private static final Integer ISDELETED_NOR = 0;

    //删除
    private static final Integer ISDELETED_DEL = 1;

    @Override
    @Transactional
    public void addSmsAccount(SmsAccountDto dto) throws JsonProcessingException {
        long configId = Long.parseLong(
                ThreadLocalRandom.current().nextInt(100, 1000)
                        + String.valueOf(System.currentTimeMillis()));
        List<String> channelNames = dto.getChannels().stream().map(SmsChannelDto::getChannelName).collect(Collectors.toList());
        List<Long> channelIds = dto.getChannels().stream().map(SmsChannelDto::getChannelId).collect(Collectors.toList());
        for (PriceDateDTO priceDate : dto.getPriceDates()) {
            Date effectStartDate = Date.from(priceDate.getEffectStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date effectEndDate = null;
            if (priceDate.getEffectEndDate() != null) {
                effectEndDate = Date.from(priceDate.getEffectEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            MarketingSmsAccountRecord accountRecord = new MarketingSmsAccountRecord();
            accountRecord.setConfigId(configId);
            accountRecord.setVendorId(dto.getVendorId());
            accountRecord.setVendorName(dto.getVendorName());
            String channelsInfo = objectMapper.writeValueAsString(dto.getChannels());
            accountRecord.setChannelsInfo(channelsInfo);
            accountRecord.setPrice(priceDate.getPrice());
            accountRecord.setEffectStartDate(effectStartDate);
            accountRecord.setEffectEndDate(effectEndDate);
            smsAccountRecordMapper.insertSelective(accountRecord);
            for (SmsChannelDto channel : dto.getChannels()) {
                MarketingSmsAccountDetail accountDetail = new MarketingSmsAccountDetail();
                accountDetail.setConfigId(configId);
                accountDetail.setRecordId(accountRecord.getId());
                accountDetail.setVendorId(dto.getVendorId());
                accountDetail.setVendorName(dto.getVendorName());
                accountDetail.setChannelId(channel.getChannelId());
                accountDetail.setChannelName(channel.getChannelName());
                accountDetail.setPrice(priceDate.getPrice());
                accountDetail.setEffectStartDate(effectStartDate);
                accountDetail.setEffectEndDate(effectEndDate);
                smsAccountDetailMapper.insertSelective(accountDetail);
            }
        }
        MarketingSmsAccountLog accountLog = new MarketingSmsAccountLog();
        accountLog.setConfigId(configId);
        accountLog.setVendorId(dto.getVendorId());
        accountLog.setVendorName(dto.getVendorName());
        JSONObject detail = new JSONObject();
        detail.put("channelIds", objectMapper.writeValueAsString(channelIds));
        detail.put("channelNames", objectMapper.writeValueAsString(channelNames));
        detail.put("priceDates", JSON.toJSONString(dto.getPriceDates()));
        accountLog.setDetail(detail.toJSONString());
        userRecord(accountLog);
        accountLog.setOpeType(OpeTypeEnum.OPE_TYPE_INS.getType());
        smsAccountLogMapper.insertSelective(accountLog);
    }

    @Override
    @Transactional
    public void updSmsAccount(SmsAccountDto dto) throws JsonProcessingException {
        //1.删除record和detail
        MarketingSmsAccountRecordExample accountRecordExample = new MarketingSmsAccountRecordExample();
        accountRecordExample.createCriteria().andConfigIdEqualTo(dto.getConfigId());
        MarketingSmsAccountRecord updateAccountRecord = new MarketingSmsAccountRecord();
        updateAccountRecord.setIsDelete(ISDELETED_DEL);
        smsAccountRecordMapper.updateByExampleSelective(updateAccountRecord, accountRecordExample);
        MarketingSmsAccountDetailExample accountDetailExample = new MarketingSmsAccountDetailExample();
        accountDetailExample.createCriteria().andConfigIdEqualTo(dto.getConfigId());
        MarketingSmsAccountDetail updateAccountDetail = new MarketingSmsAccountDetail();
        updateAccountDetail.setIsDelete(ISDELETED_DEL);
        smsAccountDetailMapper.updateByExampleSelective(updateAccountDetail, accountDetailExample);
        //2.新增
        List<String> channelNames = dto.getChannels().stream().map(SmsChannelDto::getChannelName).collect(Collectors.toList());
        List<Long> channelIds = dto.getChannels().stream().map(SmsChannelDto::getChannelId).collect(Collectors.toList());
        for (PriceDateDTO priceDate : dto.getPriceDates()) {
            Date effectStartDate = Date.from(priceDate.getEffectStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date effectEndDate = null;
            if (priceDate.getEffectEndDate() != null) {
                effectEndDate = Date.from(priceDate.getEffectEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            MarketingSmsAccountRecord accountRecord = new MarketingSmsAccountRecord();
            accountRecord.setConfigId(dto.getConfigId());
            accountRecord.setVendorId(dto.getVendorId());
            accountRecord.setVendorName(dto.getVendorName());
            String channelsInfo = objectMapper.writeValueAsString(dto.getChannels());
            accountRecord.setChannelsInfo(channelsInfo);
            accountRecord.setPrice(priceDate.getPrice());
            accountRecord.setEffectStartDate(effectStartDate);
            accountRecord.setEffectEndDate(effectEndDate);
            smsAccountRecordMapper.insertSelective(accountRecord);
            for (SmsChannelDto channel : dto.getChannels()) {
                MarketingSmsAccountDetail accountDetail = new MarketingSmsAccountDetail();
                accountDetail.setConfigId(dto.getConfigId());
                accountDetail.setRecordId(accountRecord.getId());
                accountDetail.setVendorId(dto.getVendorId());
                accountDetail.setVendorName(dto.getVendorName());
                accountDetail.setChannelId(channel.getChannelId());
                accountDetail.setChannelName(channel.getChannelName());
                accountDetail.setPrice(priceDate.getPrice());
                accountDetail.setEffectStartDate(effectStartDate);
                accountDetail.setEffectEndDate(effectEndDate);
                smsAccountDetailMapper.insertSelective(accountDetail);
            }
        }
        MarketingSmsAccountLog accountLog = new MarketingSmsAccountLog();
        accountLog.setConfigId(dto.getConfigId());
        accountLog.setVendorId(dto.getVendorId());
        accountLog.setVendorName(dto.getVendorName());
        JSONObject detail = new JSONObject();
        detail.put("channelIds", objectMapper.writeValueAsString(channelIds));
        detail.put("channelNames", objectMapper.writeValueAsString(channelNames));
        detail.put("priceDates", JSON.toJSONString(dto.getPriceDates()));
        accountLog.setDetail(detail.toJSONString());
        userRecord(accountLog);
        accountLog.setOpeType(OpeTypeEnum.OPE_TYPE_UPD.getType());
        smsAccountLogMapper.insertSelective(accountLog);
    }

    @Override
    @Transactional
    public void forbSmsAccount(Long configId) {
        //1.禁用record
        MarketingSmsAccountRecordExample accountRecordExample = new MarketingSmsAccountRecordExample();
        accountRecordExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        MarketingSmsAccountRecord updateAccountRecord = new MarketingSmsAccountRecord();
        updateAccountRecord.setEnabled(ENABLED_FORB);
        smsAccountRecordMapper.updateByExampleSelective(updateAccountRecord, accountRecordExample);
        //2.禁用detail
        MarketingSmsAccountDetailExample accountDetailExample = new MarketingSmsAccountDetailExample();
        accountDetailExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        MarketingSmsAccountDetail updateAccountDetail = new MarketingSmsAccountDetail();
        updateAccountDetail.setEnabled(ENABLED_FORB);
        smsAccountDetailMapper.updateByExampleSelective(updateAccountDetail, accountDetailExample);
        //3.新增禁用日志
        MarketingSmsAccountLogExample accountLogExample = new MarketingSmsAccountLogExample();
        accountLogExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        accountLogExample.setOrderByClause("create_time desc limit 1");
        MarketingSmsAccountLog oldAccountLog = smsAccountLogMapper.selectByExample(accountLogExample).get(0);
        MarketingSmsAccountLog accountLog = new MarketingSmsAccountLog();
        BeanUtils.copyProperties(oldAccountLog, accountLog);
        accountLog.setId(null);
        userRecord(accountLog);
        accountLog.setOpeType(OpeTypeEnum.OPE_TYPE_FOB.getType());
        accountLog.setCreateTime(null);
        accountLog.setUpdateTime(null);
        smsAccountLogMapper.insertSelective(accountLog);
    }

    @Override
    @Transactional
    public void allowSmsAccount(Long configId) {
        //1.启用record
        MarketingSmsAccountRecordExample accountRecordExample = new MarketingSmsAccountRecordExample();
        accountRecordExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        MarketingSmsAccountRecord updateAccountRecord = new MarketingSmsAccountRecord();
        updateAccountRecord.setEnabled(ENABLED_ACT);
        smsAccountRecordMapper.updateByExampleSelective(updateAccountRecord, accountRecordExample);
        //2.启用detail
        MarketingSmsAccountDetailExample accountDetailExample = new MarketingSmsAccountDetailExample();
        accountDetailExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        MarketingSmsAccountDetail updateAccountDetail = new MarketingSmsAccountDetail();
        updateAccountDetail.setEnabled(ENABLED_ACT);
        smsAccountDetailMapper.updateByExampleSelective(updateAccountDetail, accountDetailExample);
        //3.新增启用日志
        MarketingSmsAccountLogExample accountLogExample = new MarketingSmsAccountLogExample();
        accountLogExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        accountLogExample.setOrderByClause("create_time desc limit 1");
        MarketingSmsAccountLog oldAccountLog = smsAccountLogMapper.selectByExample(accountLogExample).get(0);
        MarketingSmsAccountLog accountLog = new MarketingSmsAccountLog();
        BeanUtils.copyProperties(oldAccountLog, accountLog);
        accountLog.setId(null);
        userRecord(accountLog);
        accountLog.setOpeType(OpeTypeEnum.OPE_TYPE_ALLOW.getType());
        accountLog.setCreateTime(null);
        accountLog.setUpdateTime(null);
        smsAccountLogMapper.insertSelective(accountLog);
    }

    @Override
    @Transactional
    public void addLineAccount(LineAccountDto dto) throws JsonProcessingException {
        long configId = Long.parseLong(
                ThreadLocalRandom.current().nextInt(100, 1000)
                        + String.valueOf(System.currentTimeMillis()));
        List<String> callerFullnames = dto.getLines().stream().map(LineCallerDto::getCallerFullname).collect(Collectors.toList());
        List<Long> gatewayIds = dto.getLines().stream().map(LineCallerDto::getGatewayId).collect(Collectors.toList());
        for (PriceDateDTO priceDate : dto.getPriceDates()) {
            Date effectStartDate = Date.from(priceDate.getEffectStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date effectEndDate = null;
            if (priceDate.getEffectEndDate() != null) {
                effectEndDate = Date.from(priceDate.getEffectEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            MarketingLineAccountRecord accountRecord = new MarketingLineAccountRecord();
            accountRecord.setConfigId(configId);
            accountRecord.setLineSupplier(dto.getLineSupplier());
            String linesInfo = objectMapper.writeValueAsString(dto.getLines());
            accountRecord.setLinesInfo(linesInfo);
            accountRecord.setPrice(priceDate.getPrice());
            accountRecord.setEffectStartDate(effectStartDate);
            accountRecord.setEffectEndDate(effectEndDate);
            lineAccountRecordMapper.insertSelective(accountRecord);
            for (LineCallerDto line : dto.getLines()) {
                MarketingLineAccountDetail accountDetail = new MarketingLineAccountDetail();
                accountDetail.setConfigId(configId);
                accountDetail.setRecordId(accountRecord.getId());
                accountDetail.setLineSupplier(dto.getLineSupplier());
                accountDetail.setGatewayId(line.getGatewayId());
                accountDetail.setCallerFullname(line.getCallerFullname());
                accountDetail.setPrice(priceDate.getPrice());
                accountDetail.setEffectStartDate(effectStartDate);
                accountDetail.setEffectEndDate(effectEndDate);
                lineAccountDetailMapper.insertSelective(accountDetail);
            }
        }
        MarketingLineAccountLog accountLog = new MarketingLineAccountLog();
        accountLog.setConfigId(configId);
        accountLog.setLineSupplier(dto.getLineSupplier());
        JSONObject detail = new JSONObject();
        detail.put("gatewayIds", objectMapper.writeValueAsString(gatewayIds));
        detail.put("callerFullnames", objectMapper.writeValueAsString(callerFullnames));
        detail.put("priceDates", JSON.toJSONString(dto.getPriceDates()));
        accountLog.setDetail(detail.toJSONString());
        userRecord(accountLog);
        accountLog.setOpeType(OpeTypeEnum.OPE_TYPE_INS.getType());
        lineAccountLogMapper.insertSelective(accountLog);
    }

    @Override
    @Transactional
    public void updLineAccount(LineAccountDto dto) throws JsonProcessingException {
        //1.删除record和detail
        MarketingLineAccountRecordExample accountRecordExample = new MarketingLineAccountRecordExample();
        accountRecordExample.createCriteria().andConfigIdEqualTo(dto.getConfigId());
        MarketingLineAccountRecord updateAccountRecord = new MarketingLineAccountRecord();
        updateAccountRecord.setIsDelete(ISDELETED_DEL);
        lineAccountRecordMapper.updateByExampleSelective(updateAccountRecord, accountRecordExample);
        MarketingLineAccountDetailExample accountDetailExample = new MarketingLineAccountDetailExample();
        accountDetailExample.createCriteria().andConfigIdEqualTo(dto.getConfigId());
        MarketingLineAccountDetail updateAccountDetail = new MarketingLineAccountDetail();
        updateAccountDetail.setIsDelete(ISDELETED_DEL);
        lineAccountDetailMapper.updateByExampleSelective(updateAccountDetail, accountDetailExample);
        //2.新增
        List<String> callerFullnames = dto.getLines().stream().map(LineCallerDto::getCallerFullname).collect(Collectors.toList());
        List<Long> gatewayIds = dto.getLines().stream().map(LineCallerDto::getGatewayId).collect(Collectors.toList());
        for (PriceDateDTO priceDate : dto.getPriceDates()) {
            Date effectStartDate = Date.from(priceDate.getEffectStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date effectEndDate = null;
            if (priceDate.getEffectEndDate() != null) {
                effectEndDate = Date.from(priceDate.getEffectEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            MarketingLineAccountRecord accountRecord = new MarketingLineAccountRecord();
            accountRecord.setConfigId(dto.getConfigId());
            accountRecord.setLineSupplier(dto.getLineSupplier());
            String linesInfo = objectMapper.writeValueAsString(dto.getLines());
            accountRecord.setLinesInfo(linesInfo);
            accountRecord.setPrice(priceDate.getPrice());
            accountRecord.setEffectStartDate(effectStartDate);
            accountRecord.setEffectEndDate(effectEndDate);
            lineAccountRecordMapper.insertSelective(accountRecord);
            for (LineCallerDto line : dto.getLines()) {
                MarketingLineAccountDetail accountDetail = new MarketingLineAccountDetail();
                accountDetail.setConfigId(dto.getConfigId());
                accountDetail.setRecordId(accountRecord.getId());
                accountDetail.setLineSupplier(dto.getLineSupplier());
                accountDetail.setGatewayId(line.getGatewayId());
                accountDetail.setCallerFullname(line.getCallerFullname());
                accountDetail.setPrice(priceDate.getPrice());
                accountDetail.setEffectStartDate(effectStartDate);
                accountDetail.setEffectEndDate(effectEndDate);
                lineAccountDetailMapper.insertSelective(accountDetail);
            }
        }
        MarketingLineAccountLog accountLog = new MarketingLineAccountLog();
        accountLog.setConfigId(dto.getConfigId());
        accountLog.setLineSupplier(dto.getLineSupplier());
        JSONObject detail = new JSONObject();
        detail.put("gatewayIds", objectMapper.writeValueAsString(gatewayIds));
        detail.put("callerFullnames", objectMapper.writeValueAsString(callerFullnames));
        detail.put("priceDates", JSON.toJSONString(dto.getPriceDates()));
        accountLog.setDetail(detail.toJSONString());
        userRecord(accountLog);
        accountLog.setOpeType(OpeTypeEnum.OPE_TYPE_UPD.getType());
        lineAccountLogMapper.insertSelective(accountLog);
    }

    @Override
    @Transactional
    public void forbLineAccount(Long configId) {
        //1.禁用record
        MarketingLineAccountRecordExample accountRecordExample = new MarketingLineAccountRecordExample();
        accountRecordExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        MarketingLineAccountRecord updateAccountRecord = new MarketingLineAccountRecord();
        updateAccountRecord.setEnabled(ENABLED_FORB);
        lineAccountRecordMapper.updateByExampleSelective(updateAccountRecord, accountRecordExample);
        //2.禁用detail
        MarketingLineAccountDetailExample accountDetailExample = new MarketingLineAccountDetailExample();
        accountDetailExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        MarketingLineAccountDetail updateAccountDetail = new MarketingLineAccountDetail();
        updateAccountDetail.setEnabled(ENABLED_FORB);
        lineAccountDetailMapper.updateByExampleSelective(updateAccountDetail, accountDetailExample);
        //3.新增禁用日志
        MarketingLineAccountLogExample accountLogExample = new MarketingLineAccountLogExample();
        accountLogExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        accountLogExample.setOrderByClause("create_time desc limit 1");
        MarketingLineAccountLog oldAccountLog = lineAccountLogMapper.selectByExample(accountLogExample).get(0);
        MarketingLineAccountLog accountLog = new MarketingLineAccountLog();
        BeanUtils.copyProperties(oldAccountLog, accountLog);
        accountLog.setId(null);
        userRecord(accountLog);
        accountLog.setOpeType(OpeTypeEnum.OPE_TYPE_FOB.getType());
        accountLog.setCreateTime(null);
        accountLog.setUpdateTime(null);
        lineAccountLogMapper.insertSelective(accountLog);
    }

    @Override
    public void allowLineAccount(Long configId) {
        //1.启用record
        MarketingLineAccountRecordExample accountRecordExample = new MarketingLineAccountRecordExample();
        accountRecordExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        MarketingLineAccountRecord updateAccountRecord = new MarketingLineAccountRecord();
        updateAccountRecord.setEnabled(ENABLED_ACT);
        lineAccountRecordMapper.updateByExampleSelective(updateAccountRecord, accountRecordExample);
        //2.启用detail
        MarketingLineAccountDetailExample accountDetailExample = new MarketingLineAccountDetailExample();
        accountDetailExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        MarketingLineAccountDetail updateAccountDetail = new MarketingLineAccountDetail();
        updateAccountDetail.setEnabled(ENABLED_ACT);
        lineAccountDetailMapper.updateByExampleSelective(updateAccountDetail, accountDetailExample);
        //3.新增启用日志
        MarketingLineAccountLogExample accountLogExample = new MarketingLineAccountLogExample();
        accountLogExample.createCriteria().andConfigIdEqualTo(configId).andIsDeleteEqualTo(0);
        accountLogExample.setOrderByClause("create_time desc limit 1");
        MarketingLineAccountLog oldAccountLog = lineAccountLogMapper.selectByExample(accountLogExample).get(0);
        MarketingLineAccountLog accountLog = new MarketingLineAccountLog();
        BeanUtils.copyProperties(oldAccountLog, accountLog);
        accountLog.setId(null);
        userRecord(accountLog);
        accountLog.setOpeType(OpeTypeEnum.OPE_TYPE_ALLOW.getType());
        accountLog.setCreateTime(null);
        accountLog.setUpdateTime(null);
        lineAccountLogMapper.insertSelective(accountLog);
    }

    private void userRecord(MarketingSmsAccountLog accountLog) {
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        if (userDetail != null) {
            accountLog.setUserId(Long.valueOf(userDetail.getId()));
            accountLog.setUserName(userDetail.getUserName());
            accountLog.setRealName(userDetail.getRealName());
        }
    }

    private void userRecord(MarketingLineAccountLog accountLog) {
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        if (userDetail != null) {
            accountLog.setUserId(Long.valueOf(userDetail.getId()));
            accountLog.setUserName(userDetail.getUserName());
            accountLog.setRealName(userDetail.getRealName());
        }
    }

}
