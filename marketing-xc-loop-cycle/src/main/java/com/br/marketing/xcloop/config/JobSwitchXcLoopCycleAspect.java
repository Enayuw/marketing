package com.br.marketing.xcloop.config;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * xc-loop-cycle服务上线时Job开关切面
 *
 * @author dongshuo.he
 */
@Component
@Aspect
@Slf4j
public class JobSwitchXcLoopCycleAspect {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Around("execution(* com.br.marketing.xcloop.job..*.process(..))")
    public void handleJobSwitch(ProceedingJoinPoint jp) throws Throwable {
        //开关默认关闭
        boolean JobOnlineSwitch = StringUtils.isNotEmpty(marketingCommonConfig.getXcLoopCycleJobOnlineSwitch()) ? marketingCommonConfig.getXcLoopCycleJobOnlineSwitch() : false;
        //未开启开关，正常执行
        if (!JobOnlineSwitch) {
            jp.proceed();
        } else {
            log.warn("xc-loop-cycle正在上线，定时任务暂不执行,上线完成记得关闭开关");
        }
    }


}
