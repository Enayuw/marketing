package com.br.marketing.aspect;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.RequestOperationLog;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.InterfaceOperationsEnum;
import com.br.marketing.handle.LogSpelProcess;
import com.br.marketing.service.LogRecordService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author kongbx
 * @date 2024/4/17
 */
@Slf4j
@Aspect
@Component
public class LogRecordAspect {

    @Autowired
    private LogSpelProcess logSpelProcess;

    @Autowired
    private LogRecordService logRecordService;

    private static final ThreadPoolExecutor BR_EXECUTORS = BrExecutors.getThreadPool(50, 50);

    @Pointcut("@annotation(com.br.marketing.aspect.LogRecordAnnotation)")
    private void method() {
    }

    //举例：Prr在4,12号，20点26修改了数据包一中的原开启撞库时间4月15号 20:24:34 的设定撞得量级2,000,000修改为4月16号 20:24:34的设定撞得量级1,000,000
    @Around("method()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        return insertLog(joinPoint);
    }

    /**
     * LogRecordAspect
     * 进行插入日志
     *
     * @param joinPoint
     */
    private Object insertLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取切点方法上的注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        LogRecordAnnotation annotation = method.getAnnotation(LogRecordAnnotation.class);

        RequestOperationLog requestOperationLog = null;
        try {
            // 拼接操作日志
            requestOperationLog = this.recordLog(annotation, joinPoint);
        }catch (Exception e) {
            log.error("生成操作日志异常",e);
        }

        Object proceed = null;
        // 方法执行
        try {
            // 执行被拦截的方法,如果是系统异常那就直接抛出异常也不需要记录日志，但如果是业务异常，那就用记录这个日志是否成功
            proceed = joinPoint.proceed();

            // 仅在requestOperationLog不为null且proceed非空时设置结果
            if (requestOperationLog != null && proceed != null) {
                try {
                    String result = proceed instanceof String ? (String) proceed : JSONUtil.toJsonStr(proceed);
                    if (StringUtils.isNotEmpty(result)) {
                        requestOperationLog.setResult(result);
                    }
                } catch (Exception e) {
                    log.error("解析方法返回值并设置操作日志结果时出错", e);
                }
            }
            // 插入日志
            if(requestOperationLog != null){
                RequestOperationLog finalRequestOperationLog = requestOperationLog;
                BR_EXECUTORS.execute(() -> {
                    try {
                        logRecordService.insert(finalRequestOperationLog);
                    } catch (Exception e) {
                        log.error("目标方法执行异常",e);
                    }
                });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return proceed;
    }

    /**
     * 生成最终日志
     *
     * @param annotation 注解
     * @return joinPoint 切面
     */
    private RequestOperationLog recordLog(LogRecordAnnotation annotation, ProceedingJoinPoint joinPoint) {
        // 获取存在Spel表达式的属性
        InterfaceOperationsEnum interfaceOperationsEnum = annotation.bizNo();
        String code = interfaceOperationsEnum.getCode();
        List<String> templates = Lists.newArrayList(code,annotation.extendInfo(),annotation.originalValue());
        templates = templates.stream().filter(e -> StringUtils.isNotBlank(e)).collect(Collectors.toList());
        // 解析SPEL属性和方法
        HashMap<String, String> processMap = logSpelProcess.processBeforeExec(templates, joinPoint);
        // 解析三目运算
        HashMap<String, String> process = logSpelProcess.ternaryProcess(processMap, joinPoint);
        // 获取当前登录人信息
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        // 从当前请求上下文中获取ServletRequestAttributes对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 获取入参
        final Object[] args = joinPoint.getArgs();

        RequestOperationLog requestOperationLog = new RequestOperationLog();
        if(userDetail != null){
            requestOperationLog.setOperator(userDetail.getUserName());
        }
        requestOperationLog.setBizNo(code);
        List<Object> filteredArgs = Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest || arg instanceof HttpServletResponse))
                .collect(Collectors.toList());
        requestOperationLog.setRequestParam(JSONObject.toJSONString(filteredArgs));
        requestOperationLog.setUrl(attributes.getRequest().getRequestURI());
        requestOperationLog.setExtendInfo(process.get(annotation.extendInfo()));
        requestOperationLog.setOriginalValue(process.get(annotation.originalValue()));
        requestOperationLog.setCreateTime(new Date());
        requestOperationLog.setUpdateTime(new Date());

        return requestOperationLog;
    }

}

