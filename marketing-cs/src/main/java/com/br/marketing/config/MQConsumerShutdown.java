package com.br.marketing.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.Map;


/**
 * mq消费端下线
 *
 * @author Hua Qiang
 * @date 2024-10-08 19:26
 */
@Slf4j
@Component
public class MQConsumerShutdown {


    /**
     * 2024-10-12 11:16
     * rocketMQ 消费者停机
     * <p>
     * 如果使用spring容器的钩子函数，可不使用该方法或自已实现监听器
     */
    public void rocketmqDestroy(@NotNull ApplicationContext context) {
        if (context == null) {
            log.warn("rocketMQ消费者下线，ApplicationContext为null");
            return;
        }
        try {
            Map<String, DefaultRocketMQListenerContainer> beansOfType = context.getBeansOfType(
                    DefaultRocketMQListenerContainer.class);
            log.warn("rocketMQ消费者下线，##### DefaultRocketMQListenerContainer：{}", beansOfType);
//            Optional.ofNullable(beansOfType).ifPresent(
//                    (Map<String, DefaultRocketMQListenerContainer> map) -> map.forEach(
//                            (String k, DefaultRocketMQListenerContainer v) -> {
//                                v.stop();
//                                log.warn("rocketMQ消费者下线，监听器:{},信息:{}", k, v);
//                            }));
            if (CollectionUtils.isEmpty(beansOfType)) {
                return;
            }
            beansOfType.forEach((String k, DefaultRocketMQListenerContainer v) -> {
                v.stop();
                log.warn("rocketMQ消费者下线，监听器:{},信息:{}", k, v);
            });
        } catch (BeansException e) {
            log.error(e.getMessage(), e);
        }
    }
}
