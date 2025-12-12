package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.LineBaseShowInfoDTO;
import com.br.marketing.dto.SmsBaseShowInfoDTO;
import com.br.marketing.dto.account.LineAccountDto;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.vo.LineAccountDetailVO;
import com.br.marketing.vo.SmsAccountDetailVO;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.validation.Valid;
import java.util.List;

public interface LineSmsAccountNormalService {

    List<LineBaseShowInfoDTO> getLineAccountBasInfo();

    Result addLineAccount(@Valid LineAccountDto dto) throws JsonProcessingException;

    Result updLineAccount(LineAccountDto dto) throws JsonProcessingException;

    PageResultReturn getLineAccounts(Integer current, Integer size, String lineSupplier, String callerFullName, Double price);

    List<LineAccountDetailVO> getLineAccountsByGroupId(Long groupId);

    PageResultReturn getLineAccountLogs(Integer current, Integer size, Long groupId);

    Result forbLineAccount(Long groupId);

    Result allowLineAccount(Long groupId);

    Result deleteLineAccount(Long groupId);

    Result addSmsAccount(SmsAccountDto smsAccountDto) throws JsonProcessingException;

    Result updSmsAccount(SmsAccountDto dto) throws JsonProcessingException;

    List<SmsBaseShowInfoDTO>  getSmsAccountBaseInfo();

    Result forbSmsAccount(Long groupId);

    Result allowSmsAccount(Long groupId);

    Result deleteSmsAccount(Long groupId);

    PageResultReturn getSmsAccountLogs(Integer current, Integer size, Long groupId);

    PageResultReturn getSmsAccounts(Integer current, Integer size, Long vendorId, Long channelId, Double price);

    List<SmsAccountDetailVO> getSmsAccountsByGroupId(Long groupId);

}
