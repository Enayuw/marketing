package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.LineAccountDto;
import com.br.marketing.dto.account.SmsAccountDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.validation.Valid;

public interface LineSmsAccountDataService {

    void addSmsAccount(SmsAccountDto dto) throws JsonProcessingException;

    void updSmsAccount(SmsAccountDto dto) throws JsonProcessingException;

    void forbSmsAccount(Long configId);

    void allowSmsAccount(Long configId);

    void addLineAccount(LineAccountDto dto) throws JsonProcessingException;

    void updLineAccount(LineAccountDto dto) throws JsonProcessingException;

    void forbLineAccount(Long configId);

    void allowLineAccount(Long configId);
}
