package com.br.marketing.service.Impl.xc;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;

@Service
public class XieChengCollidingDataLogServiceImpl implements XieChengCollidingDataLogService {

    @Resource
    private XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;

    @Override
    public Result<Boolean> saveXieChengCollidingDataLog(XieChengCollidingDataLog xieChengCollidingDataLog) {
        xieChengCollidingDataLogMapper.insert(xieChengCollidingDataLog);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }
}
