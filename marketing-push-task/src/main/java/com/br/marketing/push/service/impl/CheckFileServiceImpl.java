package com.br.marketing.push.service.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.push.service.ResultCheckService;
import com.br.marketing.service.sftp.PushFinishSucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckFileServiceImpl {
    @Autowired
    ResultCheckService resultCheckService;

    @Autowired
    PushFinishSucService pushFinishSucService;

    public Result<Boolean> consumerFileCheck(Long fileId){
        pushFinishSucService.pushFinish(fileId);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }
}
