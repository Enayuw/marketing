package com.br.marketing.api.aspect;

import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.Impl.SystemExceptionServiceImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.omg.CORBA.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 对外接口的异常捕获
 */
@Component
@Order(-999)
@Aspect
public class ErrorControllerAspect {
    private static final Logger log = LoggerFactory.getLogger(ErrorControllerAspect.class);

    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    EmailService systemExceptionServiceImpl;

    /**
     * 捕获Reuslt 形式输出的接口异常
     * @param jp
     * @return
     * @throws Throwable
     */
    @Around("execution(public com.br.marketing.common.commondto.Result com.br.marketing.api.controller..*.*(..))")
    public Object handResultException(ProceedingJoinPoint jp) throws Throwable {
        try {
            Object rvt = jp.proceed();
            return rvt;
        } catch (Throwable e) {
            try {
                Result obj = new Result();
                obj.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                final MethodSignature methodSignature = (MethodSignature) jp.getSignature();
                errorHandle(methodSignature.getDeclaringType().getName(),methodSignature.getName(), jp.getArgs(), e);
                obj.setMessage("发生内部错误");
                return obj;
            } catch (Exception ee) {
                log.error("异常结果生成异常", ee);
                //无法正确生成返回结果，接着抛出异常
                throw e;
            }
        }

    }

    /**
     * 捕获ApiNoDataResult 形式输出的接口异常
     * @param jp
     * @return
     * @throws Throwable
     */
    @Around("execution(public com.br.marketing.common.commondto.ApiNoDataResult com.br.marketing.api.controller..*.*(..))")
    public Object handApiNoDataResultException(ProceedingJoinPoint jp) throws Throwable {
        try {
            Object rvt = jp.proceed();
            return rvt;
        } catch (Throwable e) {
            try {
                ApiNoDataResult obj = new ApiNoDataResult();
                obj.setCode("10001");
                final MethodSignature methodSignature = (MethodSignature) jp.getSignature();
                errorHandle(methodSignature.getDeclaringType().getName(),methodSignature.getName(), jp.getArgs(), e);
                obj.setMessage("系统错误");
                return obj;
            } catch (Exception ee) {
                log.error("异常结果生成异常", ee);
                //无法正确生成返回结果，接着抛出异常
                throw e;
            }
        }

    }

    /**
     * 异常信息处理
     * @param typeName
     * @param methodName
     * @param args
     * @param e
     */
    private void errorHandle(String typeName,String methodName,Object[] args,Throwable e){
        StringBuilder params = new StringBuilder();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                params.append(String.format("Index:%d,Data:%s \r\n",i,args[i]));
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("\r\n环境：%s" ,env));
        stringBuilder.append(String.format("\r\n方法：%s.%s",typeName,methodName));
        stringBuilder.append(String.format("\r\n参数：%s",params.toString()));
        stringBuilder.append(String.format("\r\nException：%s", e.toString()));
        stringBuilder.append("\r\n StackTrace：");
        for (int i = 0; i < e.getStackTrace().length; i++) {
            stringBuilder.append(String.format("\r\n%s", e.getStackTrace()[i].toString()));
        }
        systemExceptionServiceImpl.sendAlarm(stringBuilder.toString(),"Marketing-Api");
        if(log.isErrorEnabled()){
            log.error(stringBuilder.toString());
        }
    }
}
