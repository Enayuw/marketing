package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.annoation.DistributeLog;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.dto.DataDistrubuteTestDTO;
import com.br.marketing.entity.MarketingCustomer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CheckServicePackageImpl {

    public String checkCsPackage(){
        return "i am new cs package".concat(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @RetryMethod(retryNowNum = 2,isOrNoDbRetry = true)
    @DistributeLog
    public Result getTestRes(DataDistrubuteTestDTO testDTO, Integer retry){
        System.out.println(JSON.toJSONString(testDTO));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }
}
