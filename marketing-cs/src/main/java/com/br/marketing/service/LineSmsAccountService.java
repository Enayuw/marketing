package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.SmsAccountDto;
import javax.validation.Valid;

public interface LineSmsAccountService {

    Result addSmsAccount(@Valid SmsAccountDto dto);

    Result updSmsAccount(SmsAccountDto dto);

    ApiResult getSmsAccountBasInfo();

    PageResultReturn getSmsAccounts(Integer current, Integer size,String vendorName,String channelsName,Double price);


    PageResultReturn getSmsAccountLogs(Integer current, Integer size, Long recordId,String vendorName,String optUserName,Integer optType);

}
