package com.br.marketing.api.aspect;

import com.br.marketing.common.annoation.SaveLog;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.CalledInterfaceLog;
import com.br.marketing.mapper.CalledInterfaceLogMapper;
import com.br.marketing.service.EmailService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

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

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    CalledInterfaceLogMapper interfaceLogMapper;

    @Autowired
    @Qualifier("logDbpool")
    ThreadPoolExecutor logDbpool;
    /**
     * 捕获Reuslt 形式输出的接口异常
     *
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
                errorHandle(methodSignature.getDeclaringType().getName(),methodSignature.getName(), jp.getArgs(), e,null);
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

        long startTime = System.currentTimeMillis();
        Method sMethod = ((MethodSignature) jp.getSignature()).getMethod();
        SaveLog saveLog = sMethod.getAnnotation(SaveLog.class);
        CalledInterfaceLog interfaceLog = new CalledInterfaceLog();
        if(saveLog!=null){

            interfaceLog.setRequestId(UUID.randomUUID().toString());
            interfaceLog.setRequestParam(jp.getArgs().toString().length()>5000
                    ?jp.getArgs().toString().substring(0,5000)
                    :jp.getArgs().toString());
            interfaceLog.setMethodName(httpServletRequest.getRequestURI());
        }

        try {
            Object rvt = jp.proceed();
            if(saveLog!=null) {
                interfaceLog.setResult(rvt.toString());
                interfaceLog.setExpire(String.valueOf(System.currentTimeMillis() - startTime));
                interfaceLog.setCreateTime(new Date());

                logDbpool.submit(() -> {
                    interfaceLogMapper.insertSelective(interfaceLog);
                });
            }
            return rvt;
        } catch (Throwable e) {
            try {
                ApiNoDataResult obj = new ApiNoDataResult();
                obj.setCode("10001");
                final MethodSignature methodSignature = (MethodSignature) jp.getSignature();
                if(saveLog != null){
                    interfaceLog.setCode(2);
                    interfaceLog.setExpire(String.valueOf(System.currentTimeMillis()-startTime));
                    interfaceLog.setCreateTime(new Date());
                }
                errorHandle(methodSignature.getDeclaringType().getName(),methodSignature.getName(), jp.getArgs(), e,saveLog==null?null:interfaceLog);
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
    private void errorHandle(String typeName,String methodName,Object[] args,Throwable e,CalledInterfaceLog interfaceLog){
        StringBuilder params = new StringBuilder();
        String br = "<br/>";
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                params.append(String.format("Index:%d,Data:%s ", i, args[i])).append(br);
            }
        }
        UUID uuid = UUID.randomUUID();
        StringBuilder stringBuilder = new StringBuilder()
                .append(br).append(String.format("环境：%s", env))
                .append(br).append(String.format("logId：%s", uuid))
                .append(br).append(String.format("方法：%s.%s", typeName, methodName))
                .append(br).append(String.format("参数：%s", params.toString()))
                .append(br).append(String.format("Exception：%s", e.toString()))
                .append(br).append(" StackTrace：");
        for (int i = 0; i < e.getStackTrace().length; i++) {
            stringBuilder.append(br).append(e.getStackTrace()[i]);
        }
        StringBuilder stringBuilderMail = new StringBuilder()
                .append(br).append(String.format("环境：%s", env))
                .append(br).append(String.format("logId：%s", uuid))
                .append(br).append(String.format("方法：%s.%s", typeName, methodName))
                .append(br).append(String.format("参数：%s", params.toString()));
        systemExceptionServiceImpl.sendAlarm(stringBuilderMail.toString(), "Marketing-Api");
        if (log.isErrorEnabled()) {
            log.error(stringBuilder.toString());
        }

        if(interfaceLog!=null){
            String s = stringBuilder.toString();
            interfaceLog.setResult(s.length()>=5000?s.substring(0,5000):s);
            logDbpool.submit(()->{
                interfaceLogMapper.insertSelective(interfaceLog);
            });
        }
    }
}
