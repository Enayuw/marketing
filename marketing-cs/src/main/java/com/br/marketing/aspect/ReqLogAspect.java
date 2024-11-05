package com.br.marketing.aspect;

import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.service.RequestInterfaceLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;

@Component
@Aspect
@Slf4j
public class ReqLogAspect {

    @Resource
    private RequestInterfaceLogService requestInterfaceLogService;

    /**
     * 方法
     *
     * @param
     * @return
     */
    @Pointcut("@annotation(com.br.marketing.aspect.ReqLogAnnotation)")
    public void pointCut() {

    }

    /**
     * 前置调用
     *
     * @param
     * @return
     */
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String requestURI = attributes.getRequest().getRequestURI();
        final Object[] args = jp.getArgs();
        Object arg = args[0];
        Object arg1 = args[1];
        long startTime = System.currentTimeMillis();
        Object proceed = jp.proceed();
        long endTime = System.currentTimeMillis();
        if (proceed instanceof ApiNoDataResult) {
            ApiNoDataResult res = (ApiNoDataResult) proceed;
            requestInterfaceLogService.saveLog(arg.toString(), requestURI, arg1, res, endTime - startTime);
        }
        return proceed;
    }

}
