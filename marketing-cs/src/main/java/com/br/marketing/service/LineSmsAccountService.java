package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.entity.MarketingDict;
import com.br.marketing.vo.MarketingSmsAccountLogVo;
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

    ApiResult getSmsAccountBasInfo();

    PageResultReturn getSmsAccounts(Integer current, Integer size,String vendorName,String channelsName,Double price);


    PageResultReturn getSmsAccountLogs(Integer current,Integer size,Long configId);

    List<MarketingSmsAccountRecordVo> getSmsAccountsByConfigId(Long configId);


    /**
     * 根据字典类别名称获取字典信息，封装为Map<dictType, List<MarketingDict>>
     * @return Map<String, List<MarketingDict>>
     */
    Map<String, List<MarketingDict>> getDictInfo(String dictType);

}
