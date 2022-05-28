package com.br.marketing.service.Impl;

import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.service.IPhoneSaleExtendInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PhoneSaleExtendInfoServiceImpl implements IPhoneSaleExtendInfoService {

    @Autowired
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Override
    public boolean isRepeatPhone(String phone, String dxType, String date) {

        int phoneCount = phoneSaleExtendInfoMapper.countByPhoneAndType(phone, dxType, date);

        return phoneCount > 0 ? true : false;
    }
}
