package com.br.marketing.sync.aspect;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * check服务上线时Job开关切面
 *
 * @author zhen.li
 */
@Component
@Aspect
@Slf4j
public class JobSwitchAspect {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Around("execution(* com.br.marketing.sync.job..*.process(..))")
    public void handleJobSwitch(ProceedingJoinPoint jp) throws Throwable {
        //开关默认关闭
        boolean JobOnlineSwitch = Boolean.TRUE.equals(marketingCommonConfig.getSyncJobOnlineSwitch());
        //未开启开关，正常执行
        if (!JobOnlineSwitch) {
            jp.proceed();
        } else {
            log.warn("正在上线，定时任务暂不执行,上线完成记得关闭开关");
        }
    }


}
