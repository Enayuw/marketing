package com.br.marketing.api.aspect;

import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
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

@Component
@Order(-999)
@Aspect
public class ErrorControllerAspect {
    private static final Logger log = LoggerFactory.getLogger(ErrorControllerAspect.class);

    @Value("${spring.profiles.active}")
    private String env;

    @Around("execution(public com.br.marketing.common.commondto.Result com.br.marketing.api.controller..*.*(..))")
    public Object handResultException(ProceedingJoinPoint jp) throws Throwable {
        try {
            Object rvt = jp.proceed();
            return rvt;
        } catch (Throwable e) {
            try {
                Result obj = new Result();
                obj.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());

                StringBuilder sb = new StringBuilder();
                Object[] args = jp.getArgs();
                if (args != null && args.length > 0) {
                    for (int i = 0; i < args.length; i++) {
                        sb.append("Index:" + i + ",Data:" + args[i] + "\r\n");
                    }
                }
                final MethodSignature methodSignature = (MethodSignature) jp.getSignature();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\r\n环境：" + env);
                stringBuilder.append("\r\n方法：" + methodSignature.getDeclaringType().getName() + "." + methodSignature.getName());
                stringBuilder.append("\r\n参数：" + sb.toString());
                stringBuilder.append("\r\n Exception：" + e.toString());
                stringBuilder.append("\r\n StackTrace：");
                for (int i = 0; i < e.getStackTrace().length; i++) {
                    stringBuilder.append("\r\n" + e.getStackTrace()[i].toString());
                }
                if(log.isErrorEnabled()){
                    log.error(stringBuilder.toString());
                }
                obj.setMessage("发生内部错误");
                return obj;
            } catch (Exception ee) {
                log.error("异常结果生成异常", ee);
                //无法正确生成返回结果，接着抛出异常
                throw e;
            }
        }

    }


    @Around("execution(public com.br.marketing.common.commondto.ApiNoDataResult com.br.marketing.api.controller..*.*(..))")
    public Object handApiNoDataResultException(ProceedingJoinPoint jp) throws Throwable {
        try {
            Object rvt = jp.proceed();
            return rvt;
        } catch (Throwable e) {
            try {
                ApiNoDataResult obj = new ApiNoDataResult();
                obj.setCode("10001");

                StringBuilder sb = new StringBuilder();
                Object[] args = jp.getArgs();
                if (args != null && args.length > 0) {
                    for (int i = 0; i < args.length; i++) {
                        sb.append("Index:" + i + ",Data:" + args[i] + "\r\n");
                    }
                }
                final MethodSignature methodSignature = (MethodSignature) jp.getSignature();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\r\n环境：" + env);
                stringBuilder.append("\r\n方法：" + methodSignature.getDeclaringType().getName() + "." + methodSignature.getName());
                stringBuilder.append("\r\n参数：" + sb.toString());
                stringBuilder.append("\r\n Exception：" + e.toString());
                stringBuilder.append("\r\n StackTrace：");
                for (int i = 0; i < e.getStackTrace().length; i++) {
                    stringBuilder.append("\r\n" + e.getStackTrace()[i].toString());
                }
                if(log.isErrorEnabled()){
                    log.error(stringBuilder.toString());
                }
                obj.setMessage("系统错误");
                return obj;
            } catch (Exception ee) {
                log.error("异常结果生成异常", ee);
                //无法正确生成返回结果，接着抛出异常
                throw e;
            }
        }

    }
}
