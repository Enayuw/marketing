package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.account.SmsAccountDto;

public interface LineSmsAccountService {

    Result addSmsAccount(SmsAccountDto dto);

    Result updSmsAccount(SmsAccountDto dto);
}
