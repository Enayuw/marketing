package com.br.marketing.rule.config;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 服务上线Job开关切面-RuleScheduler
 */
@Component
@Aspect
@Slf4j
public class JobSwitchRuleAspect {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Around("execution(* com.br.marketing.rule.job..*.process(..))")
    public void handleJobSwitch(ProceedingJoinPoint jp) throws Throwable {
        //开关默认关闭
        Boolean ruleSchedulerJobOnlineSwitch = marketingCommonConfig.getRuleSchedulerJobOnlineSwitch();
        boolean JobOnlineSwitch = StringUtils.isNotEmpty(ruleSchedulerJobOnlineSwitch) ? ruleSchedulerJobOnlineSwitch : false;
        //未开启开关，正常执行
        if (!JobOnlineSwitch) {
            jp.proceed();
        } else {
            log.warn("正在上线，定时任务暂不执行,上线完成记得关闭开关");
        }
    }


}
