package com.br.marketing.monkey.config;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 服务上线时Job开关切面-Monkey
 */
@Component
@Aspect
@Slf4j
public class JobSwitchMonkeyAspect {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 记录开关第一次开启的时间
     */
    private static volatile Long firstSwitchOnTime = null;

    @Around("execution(* com.br.marketing.monkey.job..*.process(..))")
    public void handleJobSwitch(ProceedingJoinPoint jp) throws Throwable {
        //开关默认关闭
        Boolean dataMonkeyJobOnlineSwitch = marketingCommonConfig.getDataMonkeyJobOnlineSwitch();
        boolean JobOnlineSwitch = StringUtils.isNotEmpty(dataMonkeyJobOnlineSwitch) ? dataMonkeyJobOnlineSwitch : false;
        //未开启开关，正常执行
        if (!JobOnlineSwitch) {
            // 开关关闭时重置时间
            firstSwitchOnTime = null;
            jp.proceed();
        } else {
            // 记录第一次开启的时间
            if (firstSwitchOnTime == null) {
                firstSwitchOnTime = System.currentTimeMillis();
            }

            // 检查是否超过半小时（30分钟 = 30 * 60 * 1000毫秒）
            long elapsedTime = System.currentTimeMillis() - firstSwitchOnTime;
            long halfHourInMillis = 30 * 60 * 1000;

            if (elapsedTime > halfHourInMillis) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_ERROR.getCode(), "Monkey未关闭开关"));
            } else {
                log.warn("正在上线，定时任务暂不执行,上线完成记得关闭开关");
            }
        }
    }


}
