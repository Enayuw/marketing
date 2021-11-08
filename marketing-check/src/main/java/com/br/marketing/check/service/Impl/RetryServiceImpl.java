package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.RetryDetailLog;
import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.entity.RetryMainLogExample;
import com.br.marketing.mapper.RetryDetailLogMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RetryServiceImpl {

    @Autowired
    RetryMainLogMapper retryMainLogMapper;

    @Autowired
    RetryDetailLogMapper retryDetailLogMapper;

    public void retry(){

        RetryMainLogExample mainLogExample = new RetryMainLogExample();
        mainLogExample.createCriteria()
                .andRetryStatusEqualTo(1);

        List<RetryMainLog> retryMainLogs = retryMainLogMapper.selectByExample(mainLogExample);
        for (RetryMainLog retryMainLog : retryMainLogs) {
            RetryMainLog updateMainLog = new RetryMainLog();
            updateMainLog.setId(retryMainLog.getId());
            Result result = new Result();
            if(Integer.valueOf(1).equals(retryMainLog.getRetryType())){
                 result = retryInnerService(retryMainLog);
            }
            updateMainLog.setRetryNum(retryMainLog.getRetryNum()+1);
            if(ResultCode.SUCCESS.getValue().equals(result.getCode())){
                updateMainLog.setRetryStatus(2);
            }else{
                if(updateMainLog.getRetryNum()>=retryMainLog.getRetryMaxNum()){
                    retryMainLog.setRetryStatus(3);
                }
            }
            retryMainLogMapper.updateByPrimaryKeySelective(updateMainLog);
        }

    }

    public Result retryInnerService(RetryMainLog retryMainLog){
        Result objectResult = new Result<>();
        String retryMethod = retryMainLog.getRetryMethod();
        String retryService = retryMainLog.getRetryService();
        String retryParam = retryMainLog.getRetryParam();
        RetryDetailLog detailLog = new RetryDetailLog();
        detailLog.setMainId(retryMainLog.getId());
        try {
            Class<?>  paramType = Class.forName(retryMainLog.getRetryParamType());
            Object o = JSON.parseObject(retryParam, paramType);
            if(!CkeckApplication.ac.containsBean(retryService)){
                throw new RuntimeException("找不到对应的bean");
            }
            Object bean = CkeckApplication.ac.getBean(retryService);
            Method method =  bean.getClass().getMethod(retryMethod, paramType);
            Result result = (Result) method.invoke(bean, o);
            if(ResultCode.SUCCESS.getValue().equals(result.getCode())){
                objectResult.setCode(ResultCode.SUCCESS.getValue());
                detailLog.setRetryStatus(1);
            }else{
                objectResult.setCode(ResultCode.FAIL.getValue());
                detailLog.setRetryStatus(2);
            }
            detailLog.setRetryResult(JSON.toJSONString(result));
        }
        catch (Exception ex) {
            log.error(ex.getMessage(),ex);
            objectResult.setCode(ResultCode.FAIL.getValue());
            detailLog.setRetryStatus(2);
            detailLog.setRetryResult(ex.getMessage());
        }
        retryDetailLogMapper.insertSelective(detailLog);
        return objectResult;
    }
}
