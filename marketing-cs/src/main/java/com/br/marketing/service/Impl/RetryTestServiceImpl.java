package com.br.marketing.service.Impl;

import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.service.RetryTestService;
import org.springframework.stereotype.Service;

@Service
public class RetryTestServiceImpl implements RetryTestService {

    @Override
    @RetryMethod
    public Result ret(Integer pa,Integer retry) {
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.TRUE);
    }
}
