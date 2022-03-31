package com.br.marketing.service.Impl;

import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.service.RetryTestService;
import org.springframework.stereotype.Service;

@Service
public class RetryTestServiceImpl implements RetryTestService {

    @Override
    @RetryMethod(retryNowNum = 3)
    public Result ret(Integer pa,Integer retry) {
        if(pa.equals(1)){
            System.out.println("调用方法啦-异常立即重试");
            throw new RuntimeException("错误，需要重试");
        }
        //500立即重试
        if(pa.equals(2)){
            System.out.println("调用方法啦-500立即重试");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        if(pa.equals(3)){
            System.out.println("调用方法啦-code非500不需要重试");
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.TRUE);
        }
        System.out.println("调用方法啦-正常不需要重试");
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }
}
