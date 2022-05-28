package com.br.marketing.service.Impl;

import com.br.marketing.mapper.ShuheBlackPhoneRecordMapper;
import com.br.marketing.service.IShuheBlackPhoneRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShuheBlackPhoneRecordServiceImpl implements IShuheBlackPhoneRecordService {

    @Autowired
    ShuheBlackPhoneRecordMapper shuheBlackPhoneRecordMapper;

    @Override
    public boolean isRepeatPhone(String phone, String date) {

        int phoneCount = shuheBlackPhoneRecordMapper.countByPhoneAndDate(phone, date);

        return phoneCount > 0 ? true : false;
    }
}
