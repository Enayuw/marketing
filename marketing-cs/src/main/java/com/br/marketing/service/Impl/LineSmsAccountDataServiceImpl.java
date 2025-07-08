package com.br.marketing.service.Impl;

import com.br.marketing.dto.account.PriceDateDTO;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.dto.account.SmsChannelDto;
import com.br.marketing.entity.MarketingSmsAccountLog;
import com.br.marketing.entity.MarketingSmsAccountRecord;
import com.br.marketing.mapper.MarketingSmsAccountRecordMapper;
import com.br.marketing.service.LineSmsAccountDataService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class LineSmsAccountDataServiceImpl implements LineSmsAccountDataService {

    @Resource
    MarketingSmsAccountRecordMapper smsAccountRecordMapper;

    @Override
    @Transactional
    public void addSmsAccount(SmsAccountDto dto) {
        String s = ThreadLocalRandom.current().nextInt(10000, 100000) + String.valueOf(System.currentTimeMillis());


        List<String> channelNames = dto.getChannels().stream().map(SmsChannelDto::getChannelName).collect(Collectors.toList());
        for (PriceDateDTO priceDate : dto.getPriceDates()) {
            MarketingSmsAccountRecord record = new MarketingSmsAccountRecord();
            record.setVendorName(dto.getVendorName());
//            record.setChannelsName(String.join(",", channelNames));
            record.setPrice(priceDate.getPrice());
            record.setEffectStartDate(Date.from(priceDate.getEffectStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            if (priceDate.getEffectEndDate() != null) {
                record.setEffectEndDate(Date.from(priceDate.getEffectEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            int recordId = smsAccountRecordMapper.insertSelective(record);
            MarketingSmsAccountLog log = new MarketingSmsAccountLog();
//            log.setRecordId((long) recordId);
            log.setOpeType(1);
            BeanUtils.copyProperties(record, log);
            for (SmsChannelDto channel : dto.getChannels()) {

            }

        }

    }

}
