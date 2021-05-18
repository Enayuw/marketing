package com.br.marketing.api.aspect.strategy.batch.put;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.api.aspect.AroundAspectProceeding;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.utils.ReturnUtil;
import com.br.marketing.common.constants.web.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 策略权限验证切面
 *
 * @author Wang Weiwei
 * @since 2018/3/15
 */
@Aspect
@Component
@Order(110)
@Slf4j
public class JsonParseValidator implements AroundAspectProceeding {

    @Around("com.br.marketing.api.aspect.strategy.StrategyBussinessJoinPoint.put()")
    @Override
    public void proceeding(ProceedingJoinPoint joinPoint) throws Throwable {
        Paramter paramter = new Paramter(joinPoint).invoke();
        StrategyApiContext context = paramter.getContext();
        Result result = paramter.getResult();
        Object jsonObject;
        try {
            jsonObject = context.translatejsonArray();
        } catch (Exception e) {
            log.warn("Exception",e);
            result.putCode(ResponseCode.MISS_JSON);
            ReturnUtil.returnError(context,result);
            return;
        }
        if (jsonObject == null) {
            result.putCode(ResponseCode.MISS_JSON);
            ReturnUtil.returnError(context,result);
            return;
        }
        joinPoint.proceed();
    }
}
