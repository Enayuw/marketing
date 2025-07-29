package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.LineAccountDto;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.entity.MarketingDict;
import com.br.marketing.entity.MarketingLineAccountRecord;
import com.br.marketing.vo.MarketingLineAccountRecordVO;
import com.br.marketing.vo.MarketingSmsAccountRecordVo;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface LineSmsAccountService {

    Result addSmsAccount(@Valid SmsAccountDto dto) throws JsonProcessingException;

    Result updSmsAccount(SmsAccountDto dto) throws IOException;

    Result forbSmsAccount(Long configId);

    Result allowSmsAccount(Long configId);

    ApiResult getSmsAccountBasInfo();

    PageResultReturn getSmsAccounts(Integer current, Integer size,String vendorName,String channelsName,Double price);


    PageResultReturn getSmsAccountLogs(Integer current,Integer size,Long configId);

    List<MarketingSmsAccountRecordVo> getSmsAccountsByConfigId(Long configId);

    Result addLineAccount(@Valid LineAccountDto dto) throws JsonProcessingException;

    Result updLineAccount(LineAccountDto dto) throws IOException;

    Result forbLineAccount(Long configId);

    Map<String, List<MarketingDict>> getDictInfo(String dictType);

    ApiResult getLineAccountBasInfo();

    List<MarketingLineAccountRecordVO> getLineAccountsByConfigId(Long configId);

    PageResultReturn getLineAccounts(Integer current, Integer size, String lineSupplier,String callerFullName, Double price);

    PageResultReturn getLineAccountLogs(Integer current, Integer size, Long configId);

    Result allowLineAccount(Long configId);
}
