package com.br.marketing.service;

import com.br.marketing.dto.account.LineAccountDto;
import com.br.marketing.dto.account.SmsAccountDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface LineSmsAccountDataNormalService {

    void addLineAccount(LineAccountDto dto) throws JsonProcessingException;

    void updLineAccount(LineAccountDto dto) throws JsonProcessingException;


    void forbLineAccount(Long groupId);

    void allowLineAccount(Long groupId);

    void deleteLineAccount(Long groupId);

    void forbSmsAccount(Long groupId);

    void allowSmsAccount(Long groupId);

    void deleteSmsAccount(Long groupId);

    void addSmsAccount(SmsAccountDto dto) throws JsonProcessingException;

    void updSmsAccount(SmsAccountDto dto) throws JsonProcessingException;
}
