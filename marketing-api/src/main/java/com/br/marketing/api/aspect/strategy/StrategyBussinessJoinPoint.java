package com.br.marketing.api.aspect.strategy;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/** 策略业务服务切点定义
 * @author Wang Weiwei
 * @since 2018/3/12
 */
@Aspect
@Component
public class StrategyBussinessJoinPoint {

    /**
     * 策略业务服务信息推送方法切面
     * */
    @Pointcut(value = "execution(public * com.br.marketing.api.bussinesses.Strategy*.put(..))")
    public void put(){}

    /**
     * 策略业务服务信息查询方法切面
     * */
    @Pointcut(value = "execution(public * com.br.marketing.api.bussinesses.Strategy*.query(..))")
    public void query(){}

    /**
     * 单条策略业务服务信息查询方法切面
     * */
    @Pointcut(value = "execution(public * com.br.marketing.api.bussinesses.Single*.query(..))")
    public void singleQuery(){}
}
