package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.SmsAccountDto;

import javax.validation.Valid;

public interface LineSmsAccountDataService {

    public void addSmsAccount(SmsAccountDto dto);

}
