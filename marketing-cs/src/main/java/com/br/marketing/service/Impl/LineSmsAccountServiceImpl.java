package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.service.LineSmsAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LineSmsAccountServiceImpl implements LineSmsAccountService {

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountServiceImpl.class);

    @Override
    public Result addSmsAccount(SmsAccountDto dto) {
        return null;
    }


    @Override
    public Result updSmsAccount(SmsAccountDto dto) {
        return null;
    }
}
