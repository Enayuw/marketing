package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.account.PriceDateDTO;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.dto.account.SmsChannelDto;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.OpeTypeEnum;
import com.br.marketing.mapper.MarketingSmsAccountDetailMapper;
import com.br.marketing.mapper.MarketingSmsAccountLogMapper;
import com.br.marketing.mapper.MarketingSmsAccountRecordMapper;
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
                ThreadLocalRandom.current().nextInt(1000, 10000)
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
            int recordId = smsAccountRecordMapper.insertSelective(accountRecord);
            for (SmsChannelDto channel : dto.getChannels()) {
                MarketingSmsAccountDetail accountDetail = new MarketingSmsAccountDetail();
                accountDetail.setConfigId(configId);
                accountDetail.setRecordId((long) recordId);
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
            int recordId = smsAccountRecordMapper.insertSelective(accountRecord);
            for (SmsChannelDto channel : dto.getChannels()) {
                MarketingSmsAccountDetail accountDetail = new MarketingSmsAccountDetail();
                accountDetail.setConfigId(dto.getConfigId());
                accountDetail.setRecordId((long) recordId);
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

    private void userRecord(MarketingSmsAccountLog accountLog) {
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        if (userDetail != null) {
            accountLog.setUserId(Long.valueOf(userDetail.getId()));
            accountLog.setUserName(userDetail.getUserName());
            accountLog.setRealName(userDetail.getRealName());
        }
    }

}
