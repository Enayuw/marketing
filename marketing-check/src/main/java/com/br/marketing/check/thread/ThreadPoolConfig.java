package com.br.marketing.check.thread;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/1/5 11:02
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Bean(name = "xieChengThreadPool")
    public ThreadPoolTaskExecutor xieChengThreadPool() {
        Integer xieChengDateSendThread = marketingCommonConfig.getXiechengDateSendThread();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数,它是可以同时被执行的线程数量
        executor.setCorePoolSize(xieChengDateSendThread);
        // 设置最大线程数,缓冲队列满了之后会申请超过核心线程数的线程
        executor.setMaxPoolSize(xieChengDateSendThread);
        // 设置缓冲队列容量,在执行任务之前用于保存任务
        executor.setQueueCapacity(200);
        // 设置线程生存时间（秒）,当超过了核心线程出之外的线程在生存时间到达之后会被销毁
        executor.setKeepAliveSeconds(60);
        // 设置线程名称前缀
        executor.setThreadNamePrefix("br-statistic-xieCheng-pool-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //初始化
        executor.initialize();
        return executor;
    }
}
