package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.SmsAccountDto;

public interface LineSmsAccountService {

    Result addSmsAccount(SmsAccountDto dto);

    Result updSmsAccount(SmsAccountDto dto);

    ApiResult getSmsAccountBasInfo();

    PageResultReturn getSmsAccounts(Integer current, Integer size, String smsVendor);


    PageResultReturn getSmsAccountLogs(Integer current, Integer size, Long recordId,String vendorName,String optUserName,Integer optType);

}
