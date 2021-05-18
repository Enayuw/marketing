package com.br.marketing.api.aspect.alarm;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.alarm.AlarmApiClient;
import com.br.marketing.common.utils.net.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by Bairong on 2018/12/12.
 */
@Aspect
@Component
@Slf4j
public class AlarmJoinPoint {
    @Resource
    private AlarmApiClient alarmClient;

    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;

    @Value("${otherConfig.alarm.sendCode:00}")
    private String sendCode;

    /**
     * Alarm point.
     */
    @Pointcut("execution(public * com.br.marketing.api.client.*.*(..))")
    public void alarmPoint(){}

    /**
     * Handle service method exception.
     *
     * @param joinPoint the join point
     * @param t         the t
     */
    @AfterThrowing(pointcut = "alarmPoint()", throwing = "t")
    public void handleServiceMethodException(JoinPoint joinPoint, Throwable t) {
        JSONObject json= contentToString(joinPoint,t);
        alarmClient.sendAlarm(json.toString(),"流失预警api调用外部服务报警","marketing",secretKey,sendCode);
    }
    private JSONObject contentToString(JoinPoint joinPoint, Throwable t){
        String methodName = joinPoint.getSignature().toLongString();
        JSONObject json=new JSONObject();
        json.put("host", IpUtil.getHostName());
        json.put("port", "18704");
        json.put("callMethod",methodName);
        json.put("serverName", "LOAN-WARNING-API");
        json.put("type", "OUTERFACE_SYS");
        json.put("message", t.getMessage());
        json.put("data", null);
        json.put("errorDetail", errorToString(t));
        return json;
    }

    private String errorToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        sb.append(e.getMessage()).append("</br>");
        for (int index = 0; index < stackTraceElements.length; index++) {
            sb.append("       ").append(stackTraceElements[index].getClassName()).append(".");
            sb.append(stackTraceElements[index].getMethodName());
            sb.append("(").append(stackTraceElements[index].getFileName()).append(":")
                    .append(stackTraceElements[index].getLineNumber()).append(")</br>");
        }
        return  sb.toString();
    }
}
