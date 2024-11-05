package com.br.marketing.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;


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
            log.warn("rocketMQ消费者下线All，DefaultRocketMQListenerContainer：{}", beansOfType);
            Optional.ofNullable(beansOfType).ifPresent(
                    (Map<String, DefaultRocketMQListenerContainer> map) -> map.forEach(
                            (String k, DefaultRocketMQListenerContainer v) -> {
                                long startTime = System.currentTimeMillis();
                                log.warn("rocketMQ消费者[{}]下线start，信息:{}", k, v);
                                v.stop();
                                long endTime = System.currentTimeMillis();
                                log.warn("rocketMQ消费者[{}]下线end，耗时：{}s，信息:{}", k, ((endTime - startTime) / 1000), v);
                            }));
        } catch (BeansException e) {
            log.error(e.getMessage(), e);
        }
    }
}
