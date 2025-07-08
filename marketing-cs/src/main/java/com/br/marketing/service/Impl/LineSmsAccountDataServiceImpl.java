package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.account.PriceDateDTO;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.dto.account.SmsChannelDto;
import com.br.marketing.entity.MarketingSmsAccountDetail;
import com.br.marketing.entity.MarketingSmsAccountLog;
import com.br.marketing.entity.MarketingSmsAccountRecord;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.OpeTypeEnum;
import com.br.marketing.mapper.MarketingSmsAccountDetailMapper;
import com.br.marketing.mapper.MarketingSmsAccountLogMapper;
import com.br.marketing.mapper.MarketingSmsAccountRecordMapper;
import com.br.marketing.service.LineSmsAccountDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Override
    @Transactional
    public void addSmsAccount(SmsAccountDto dto) throws JsonProcessingException {
        long configId = Long.parseLong(
                ThreadLocalRandom.current().nextInt(1000, 10000)
                        + String.valueOf(System.currentTimeMillis()));
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> channelNames = dto.getChannels().stream().map(SmsChannelDto::getChannelName).collect(Collectors.toList());
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
        accountLog.setVendorName(dto.getVendorName());
        String priceDates = objectMapper.writeValueAsString(dto.getPriceDates());
        JSONObject detail = new JSONObject();
        detail.put("channelNames", String.join(",", channelNames));
        detail.put("priceDates", priceDates);
        accountLog.setDetail(detail.toJSONString());
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        accountLog.setUserId(Long.valueOf(userDetail.getId()));
        accountLog.setUserName(userDetail.getUserName());
        accountLog.setRealName(userDetail.getRealName());
        accountLog.setOpeType(OpeTypeEnum.OPE_TYPE_INS.getType());
        smsAccountLogMapper.insertSelective(accountLog);
    }

}
