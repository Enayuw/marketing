package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.RetryDetailLog;
import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.entity.RetryMainLogExample;
import com.br.marketing.mapper.RetryDetailLogMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RetryServiceImpl {

    final static Integer aopRetry = 1;
    @Autowired
    RetryMainLogMapper retryMainLogMapper;

    @Autowired
    RetryDetailLogMapper retryDetailLogMapper;

    public void retry(){

        Long minIdByNeedRetryData = retryMainLogMapper.getMinIdByNeedRetryData();
        if(minIdByNeedRetryData==null){
            return;
        }
        boolean exec = Boolean.TRUE;
        while(exec){
            List<RetryMainLog> retryMainLogs = retryMainLogMapper.getNeedRetryData(minIdByNeedRetryData);
            if(retryMainLogs.size()<=0){
                exec = Boolean.FALSE;
                continue;
            }
            minIdByNeedRetryData = retryMainLogs.get(retryMainLogs.size()-1).getIncrId()+1;
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
                        updateMainLog.setRetryStatus(3);
                    }
                }
                retryMainLogMapper.updateByPrimaryKeySelective(updateMainLog);
            }
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
            Object bean = null;
            if(CkeckApplication.ac.containsBean(retryService)){
                bean = CkeckApplication.ac.getBean(retryService);
            }
            if(bean == null){
                bean = CkeckApplication.ac.getBean(Class.forName(retryService));
            }
            if(bean==null){
                throw new RuntimeException("找不到对应的bean");
            }
            Method method = bean.getClass().getMethod(retryMethod, paramType);;
            Result result = (Result) method.invoke(bean, o);
            if(aopRetry.equals(retryMainLog.getServiceType())){
                method =  bean.getClass().getMethod(retryMethod, paramType,Integer.class);
                // 注解的参数值是 常量，无法通过注解优雅实现
//                RetryMethod annotation = AnnotationUtils.findAnnotation(method, RetryMethod.class);
//                InvocationHandler h = Proxy.getInvocationHandler(annotation);
//                Field hField = h.getClass().getDeclaredField("memberValues");
//                hField.setAccessible(true);
//                Map memberValues = (Map) hField.get(h);
//                memberValues.put("isRetry", 2);
                result = (Result) method.invoke(bean, o,aopRetry);
            }else{
                method =  bean.getClass().getMethod(retryMethod, paramType);
                result = (Result) method.invoke(bean, o);
            }

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
