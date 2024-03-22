package com.br.marketing.service.Impl.xc;

import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;

@Service
public class XieChengCollidingDataLogServiceImpl implements XieChengCollidingDataLogService {

    @Resource
    private XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public Result<Boolean> saveXieChengCollidingDataLog(XieChengCollidingDataLog xieChengCollidingDataLog) {
        ThreadPoolExecutor executor = BrExecutors.getThreadPool(marketingCommonConfig.getXiechengSaveCollidingLogThread(),
            marketingCommonConfig.getXiechengSaveCollidingLogThread());
        executor.submit(() -> xieChengCollidingDataLogMapper.insert(xieChengCollidingDataLog));
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }
}
