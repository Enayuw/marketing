package com.br.marketing.service;

import com.br.marketing.dto.account.LineAccountDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface LineSmsAccountDataNormalService {

    void addLineAccount(LineAccountDto dto) throws JsonProcessingException;

    void forbLineAccount(Long groupId);

    void allowLineAccount(Long groupId);
}
