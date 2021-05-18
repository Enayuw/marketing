package com.br.marketing.api.aspect.strategy.batch.query;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.api.aspect.AroundAspectProceeding;
import com.br.marketing.api.aspect.strategy.batch.put.Paramter;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.common.constants.web.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 单条参数验证
 * @author Wang Weiwei
 * @since 2018/3/22
 */
@Aspect
@Component
@Order(120)
@Slf4j
public class SingleUserInfoValidator  implements AroundAspectProceeding {

    @Around("com.br.marketing.api.aspect.strategy.StrategyBussinessJoinPoint.singleQuery()")
    @Override
    public void proceeding(ProceedingJoinPoint joinPoint) throws Throwable {
        Paramter paramter = new Paramter(joinPoint).invoke();
        StrategyApiContext context = paramter.getContext();
        Result result = paramter.getResult();
        try {
            Object jsonObject;
            try {
                jsonObject = context.translatejsonObject();
            } catch (Exception e) {
                log.warn("Exception",e);
                result.putCode(ResponseCode.MISS_JSON);
                return;
            }
            if (jsonObject == null){
                result.putCode(ResponseCode.MISS_JSON);
                return;
            }
            // 当单条参数反序列化无异常，则将其转化为批量参数
            JSONArray batchArray = new JSONArray();
            batchArray.add(jsonObject);
            context.setJsonData(batchArray.toJSONString());
        }catch (Exception e){
            log.warn("Exception",e);
            result.putCode(ResponseCode.ERR_PARAM);
            return;
        }
        joinPoint.proceed();
    }
}
