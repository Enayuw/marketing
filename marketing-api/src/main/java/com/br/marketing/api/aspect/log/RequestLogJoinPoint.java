package com.br.marketing.api.aspect.log;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/** 日志记录切点定义
 * @author Wang Weiwei
 * @since 2018/3/26
 */
@Aspect
@Component
public class RequestLogJoinPoint {

    /**
     * 单条查询api
     * */
    @Pointcut("execution(public * com.br.marketing.api.web.StrategyController.query(..))")
    public void query(){}

}
