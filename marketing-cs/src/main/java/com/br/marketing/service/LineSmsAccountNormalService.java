package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.LineBaseShowInfoDto;
import com.br.marketing.dto.account.LineAccountDto;
import com.br.marketing.vo.LineAccountDetailVO;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.validation.Valid;
import java.util.List;

public interface LineSmsAccountNormalService {

    List<LineBaseShowInfoDto> getLineAccountBasInfo();

    Result addLineAccount(@Valid LineAccountDto dto) throws JsonProcessingException;

    Result updLineAccount(LineAccountDto dto) throws JsonProcessingException;

    PageResultReturn getLineAccounts(Integer current, Integer size, String lineSupplier, String callerFullName, Double price);

    List<LineAccountDetailVO> getLineAccountsByGroupId(Long groupId);

    PageResultReturn getLineAccountLogs(Integer current, Integer size, Long groupId);

    Result forbLineAccount(Long groupId);

    Result allowLineAccount(Long groupId);

    Result deleteLineAccount(Long groupId);

}
