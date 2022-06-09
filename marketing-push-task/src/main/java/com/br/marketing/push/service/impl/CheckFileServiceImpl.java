package com.br.marketing.push.service.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.push.service.PushFinishService;
import com.br.marketing.push.service.ResultCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckFileServiceImpl {
    @Autowired
    ResultCheckService resultCheckService;

    @Autowired
    PushFinishService pushFinishService;

    public Result<Boolean> consumerFileCheck(Long fileId){
        resultCheckService.taskResultCheck(fileId);
        pushFinishService.pushFinish(fileId);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }
}
