package com.br.marketing.utils;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Pulsar消费端跳过逻辑工具类
 */
@Slf4j
@Component
public class PulsarConsumerSkipUtil {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 检查是否需要跳过业务逻辑
     * @param pulsarSubscription Pulsar订阅常量
     * @return true-跳过业务逻辑直接ACK，false-正常处理业务逻辑
     */
    public boolean shouldSkipBusinessLogic(String pulsarSubscription) {
        if (marketingCommonConfig.getPulsarConsumerSkipSwitch() == null) {
            return false;
        }
        // 直接使用PulsarSubscription常量作为配置键
        Boolean skipSwitch = marketingCommonConfig.getPulsarConsumerSkipSwitch().get(pulsarSubscription);
        log.warn("【pulsar】获取开关状态, pulsarSubscription:{}, skipSwitch:{}", pulsarSubscription, skipSwitch);
        return skipSwitch != null && skipSwitch;
    }
}
