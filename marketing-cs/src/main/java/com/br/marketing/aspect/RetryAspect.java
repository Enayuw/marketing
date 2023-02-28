package com.br.marketing.aspect;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.mapper.RetryMainLogMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Date;

@Aspect
@Order(-995) // 异常处理之内
@Component
public class RetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RetryAspect.class);

    @Resource
    RetryMainLogMapper retryMainLogMapper;

    @Autowired
    RedisChgService redisChgService;

    @Around("@annotation(com.br.marketing.common.annoation.RetryMethod)")
    public Object retry(ProceedingJoinPoint jp) throws Throwable {
        final Object[] args = jp.getArgs();
        Boolean isRetry = Boolean.FALSE;
        RetryMainLog retryMainLog = new RetryMainLog();
        retryMainLog.setRetryType(1);
        if (args.length>0) {
            retryMainLog.setRetryParamType(args[0].getClass().getName());
        }
        if(args.length>1){
            isRetry = Integer.valueOf(1).equals(args[1]);
        }
        if(isRetry){
            try {
                return jp.proceed();
            }catch (Exception ex){
                throw ex;
            }
        }else{
            String className = jp.getTarget().getClass().getName();
            Method sMethod = ((MethodSignature) jp.getSignature()).getMethod();
            RetryMethod retryMethod = sMethod.getAnnotation(RetryMethod.class);
            int nowNum = retryMethod.retryNowNum();
            boolean orNoDbRetry = retryMethod.isOrNoDbRetry();
            int i = retryMethod.retryNum();
            Boolean actionMark = Boolean.TRUE;
            Object res =null;
            while(actionMark){
                try {
                    res = jp.proceed();
                    if (res instanceof Result) {
                        Result res1 = (Result) res;
                        if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(res1.getCode())) {
                            if (nowNum <= 0 && orNoDbRetry) {
                                retryMainLog.setRetryParam(JSON.toJSONString(args[0]));
                                retryMainLog.setRetryService(className);
                                retryMainLog.setServiceType(1);
                                retryMainLog.setRetryNum(0);
                                retryMainLog.setRetryStatus(1);
                                retryMainLog.setCreateTime(new Date());
                                retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                                retryMainLog.setRetryMethod(sMethod.getName());
                                retryMainLog.setRetryMaxNum(i);
                                retryMainLogMapper.insertSelective(retryMainLog);
                            }
                        }else{
                            //返回Result对象 只要code不是500 就认为调用成功
                            actionMark = Boolean.FALSE;
                        }
                    }else{
                        //返回是非Result对象，只要不抛出异常都认为调用成功
                        actionMark = Boolean.FALSE;
                    }
                    if(nowNum<=0){
                        actionMark = Boolean.FALSE;
                    }
                }catch (Exception ex){
                    if(nowNum <= 0 && orNoDbRetry){
                        retryMainLog.setRetryParam(JSON.toJSONString(args[0]));
                        retryMainLog.setRetryService(className);
                        retryMainLog.setServiceType(1);
                        retryMainLog.setRetryNum(0);
                        retryMainLog.setRetryStatus(1);
                        retryMainLog.setCreateTime(new Date());
                        retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                        retryMainLog.setRetryMethod(sMethod.getName());
                        retryMainLog.setRetryMaxNum(i);
                        retryMainLogMapper.insertSelective(retryMainLog);
                    }
                    if(nowNum <= 0){
                        throw ex;
                    }
                }finally {
                    nowNum--;
                }
            }
            return res;
        }
    }
}
