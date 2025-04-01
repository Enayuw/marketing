package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.service.PushRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class RuleCenterServiceImpl {

    @Autowired
    PushRuleService pushRuleService;

    public Result<String> pushCustomer(PushCustomerDTO dto) {
        Result<String> res = pushRuleService.pushCustomer(dto);
        if(!ResultCode.SUCCESS.getValue().equals(res.getCode())){
            return res;
        }
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }
}
