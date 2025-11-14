package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.account.LineAccountDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.validation.Valid;

public interface LineSmsAccountNormalService {

    ApiResult getLineAccountBasInfo();

    Result addLineAccount(@Valid LineAccountDto dto) throws JsonProcessingException;

}
