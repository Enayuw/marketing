package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.entity.MarketingSmsAccountLog;
import com.br.marketing.entity.MarketingSmsAccountRecord;

import javax.validation.Valid;
import java.util.List;

public interface LineSmsAccountService {

    Result addSmsAccount(@Valid SmsAccountDto dto);

    Result updSmsAccount(SmsAccountDto dto);

    ApiResult getSmsAccountBasInfo();

    PageResultReturn getSmsAccounts(Integer current, Integer size,String vendorName,String channelsName,Double price);


    List<MarketingSmsAccountLog> getSmsAccountLogs(Long configId);

    List<MarketingSmsAccountRecord> getSmsAccountsByConfigId(Long configId);
}
