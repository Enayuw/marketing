package com.br.marketing.config;

import com.br.marketing.common.utils.BrExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * mq消费端下线
 *
 * @author Hua Qiang
 * @date 2024-10-08 19:26
 */
@Slf4j
@Component
public class MqConsumerShutdown {


    @Resource
    private ConfigurableApplicationContext applicationContext;

    /**
     * 2024-10-12 11:16
     * rocketMQ 消费者停机
     * <p>
     * 如果使用spring容器的钩子函数，可不使用该方法或自已实现监听器
     */
    public void rocketmqDestroy() {
        try {
            Map<String, DefaultRocketMQListenerContainer> drlcMap = applicationContext.getBeansOfType(
                    DefaultRocketMQListenerContainer.class);
            if (log.isInfoEnabled()) {
                log.info("rocketMQ消费者下线All，DefaultRocketMQListenerContainer：{}", drlcMap);
            }
            Optional.ofNullable(drlcMap).ifPresent(
                    (Map<String, DefaultRocketMQListenerContainer> map) -> {
                        int size = map.size();
                        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(size, size, new SynchronousQueue<>()
                                , "RocketMQ-Consumer-Shutdown");
                        CompletionService<DefaultMQPushConsumer> completionService = new ExecutorCompletionService<>(threadPool);
                        map.forEach((String containerThreadName, DefaultRocketMQListenerContainer dlc) ->
                                completionService.submit(() -> {
                                    long startTime = System.currentTimeMillis();
                                    DefaultMQPushConsumer consumer = dlc.getConsumer();
                                    log.warn("rocketMQ消费者开始暂停订阅[{}]-[{}]，信息:{}", consumer.getConsumerGroup()
                                            , containerThreadName, consumer);
                                    consumer.suspend();
                                    dlc.stop();
                                    long endTime = System.currentTimeMillis();
                                    log.warn("rocketMQ消费者组下线成功[{}]-[{}]，耗时：{}s，信息:{}", dlc.getConsumerGroup()
                                            , containerThreadName, ((endTime - startTime) / 1000), dlc);
                                    return consumer;
                                }));
                        for (int i = 0; i < size; i++) {
                            try {
                                completionService.take();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                log.warn(e.getMessage(), e);
                            }
                        }
                        threadPool.shutdown();
                    });
        } catch (BeansException e) {
            log.error(e.getMessage(), e);
        }
    }
}
