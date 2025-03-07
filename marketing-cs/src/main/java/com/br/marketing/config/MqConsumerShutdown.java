package com.br.marketing.config;

import com.br.marketing.common.utils.BrExecutors;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * mq消费端下线
 *
 * @author Hua Qiang
 * @date 2024-10-08 19:26
 */
@Slf4j
@Component
public class MqConsumerShutdown implements ApplicationListener<ContextClosedEvent> {

    @Resource
    private ConfigurableApplicationContext applicationContext;
    
    // 标记是否已经执行过关闭操作，避免重复执行
    private final AtomicBoolean shutdownExecuted = new AtomicBoolean(false);
    
    // 优雅关闭的超时时间（秒）
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 10;

    /**
     * 监听 Spring 容器关闭事件，自动执行消费者优雅下线
     * 
     * @param event Spring 容器关闭事件
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (shutdownExecuted.compareAndSet(false, true)) {
            log.info("接收到 Spring 容器关闭事件，开始执行 RocketMQ 消费者优雅下线");
            rocketmqDestroy();
        }
    }

    /**
     * RocketMQ 消费者优雅停机
     * 
     * 优化点：
     * 1. 先暂停消费，等待当前消息处理完成
     * 2. 使用 CountDownLatch 确保所有消费者都完成关闭
     * 3. 设置超时机制，避免关闭过程阻塞太久
     * 4. 添加更详细的日志，便于排查问题
     */
    public void rocketmqDestroy() {
        try {
            // 获取所有 RocketMQ 消费者容器
            Map<String, DefaultRocketMQListenerContainer> drlcMap = applicationContext.getBeansOfType(
                    DefaultRocketMQListenerContainer.class);
            
            if (log.isInfoEnabled()) {
                log.info("开始 RocketMQ 消费者优雅下线，共有消费者容器：{} 个", drlcMap.size());
            }
            
            Optional.ofNullable(drlcMap).ifPresent(
                    (Map<String, DefaultRocketMQListenerContainer> map) -> {
                        int size = map.size();
                        if (size == 0) {
                            log.info("没有找到 RocketMQ 消费者容器，无需执行下线操作");
                            return;
                        }
                        
                        // 创建线程池和完成服务
                        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(size, size, new SynchronousQueue<>(),
                                "RocketMQ-Consumer-Shutdown");
                        CompletionService<DefaultMQPushConsumer> completionService = new ExecutorCompletionService<>(threadPool);
                        
                        // 用于等待所有消费者关闭完成
                        CountDownLatch shutdownLatch = new CountDownLatch(size);
                        
                        // 提交所有消费者的关闭任务
                        map.forEach((String containerThreadName, DefaultRocketMQListenerContainer dlc) ->
                                completionService.submit(() -> {
                                    try {
                                        long startTime = System.currentTimeMillis();
                                        DefaultMQPushConsumer consumer = dlc.getConsumer();
                                        String consumerGroup = consumer.getConsumerGroup();
                                        
                                        log.warn("RocketMQ 消费者开始暂停订阅 [{}]-[{}]", consumerGroup, containerThreadName);
                                        
                                        // 1. 先暂停消费，不再接收新消息
                                        consumer.suspend();
                                        
                                        // 2. 等待当前处理中的消息完成处理（给一定的缓冲时间）
                                        log.info("等待 RocketMQ 消费者 [{}] 当前处理中的消息完成处理", consumerGroup);
                                        TimeUnit.SECONDS.sleep(5);
                                        
                                        // 3. 关闭消费者
                                        log.info("开始关闭 RocketMQ 消费者 [{}]", consumerGroup);
                                        consumer.shutdown();
                                        
                                        // 4. 销毁容器
                                        dlc.destroy();
                                        
                                        long endTime = System.currentTimeMillis();
                                        log.warn("RocketMQ 消费者组下线成功 [{}]-[{}]，耗时：{}s", consumerGroup,
                                                containerThreadName, ((endTime - startTime) / 1000));
                                        
                                        return consumer;
                                    } finally {
                                        shutdownLatch.countDown();
                                    }
                                }));
                        
                        // 等待所有消费者关闭完成或超时
                        try {
                            boolean allShutdown = shutdownLatch.await(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                            if (!allShutdown) {
                                log.warn("部分 RocketMQ 消费者关闭超时（{}秒），继续应用关闭流程", SHUTDOWN_TIMEOUT_SECONDS);
                            } else {
                                log.info("所有 RocketMQ 消费者已成功关闭");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.warn("等待 RocketMQ 消费者关闭过程被中断", e);
                        }
                        
                        // 关闭线程池
                        threadPool.shutdown();
                        try {
                            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                                threadPool.shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            threadPool.shutdownNow();
                        }
                    });
        } catch (BeansException e) {
            log.error("RocketMQ 消费者下线过程中发生异常", e);
        }
    }
}
