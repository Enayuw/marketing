package com.br.marketing.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 线程池配置
 * 在使用时在需要异步执行的方法前加上 @Asyn 注解
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/10/18
 */
@Configuration
@EnableAsync
@EnableScheduling
public class ThreadPoolConfig {
    @Value("${spring.threadpool.queueCapacity}")
    private int queueCapacity;
    @Value("${spring.threadpool.corePoolSize}")
    private int corePoolSize;
    @Value("${spring.threadpool.maxPoolSize}")
    private int maxPoolSize;
    @Value("${spring.threadpool.keepAliveSeconds}")
    private int keepAliveSeconds;
    @Value("${spring.threadpool.threadNamePrefix}")
    private String threadNamePrefix;



    private ThreadPoolTaskExecutor getThreadPoolTaskExecutor(int mqueueCapacity, int mcorePoolSize,
                                                             int mmaxPoolSize, int mkeepAliveSeconds, String mthreadNamePrefix) {
        ThreadPoolTaskExecutor poolTaskExecutor = new ThreadPoolTaskExecutor();
        poolTaskExecutor.setQueueCapacity(mqueueCapacity);
        poolTaskExecutor.setCorePoolSize(mcorePoolSize);
        poolTaskExecutor.setMaxPoolSize(mmaxPoolSize);
        poolTaskExecutor.setKeepAliveSeconds(mkeepAliveSeconds);
        poolTaskExecutor.setThreadNamePrefix(mthreadNamePrefix);
        return poolTaskExecutor;
    }

    /**
     * Config common thread thread pool task executor.
     *
     * @return the thread pool task executor
     */
    @Bean(name = "ThreadPool")
    @Primary
    public ThreadPoolTaskExecutor configCommonThread(){
        ThreadPoolTaskExecutor poolTaskExecutor = getThreadPoolTaskExecutor(queueCapacity, 30, 60, keepAliveSeconds, threadNamePrefix);
        poolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        poolTaskExecutor.setAllowCoreThreadTimeOut(true);
        poolTaskExecutor.initialize();
        return poolTaskExecutor;
    }

    /**
     * Config batch thread thread pool task executor.
     *
     * @return the thread pool task executor
     */
    @Bean(name = "BatchThreadPool")
    public ThreadPoolTaskExecutor configBatchThread(){
        ThreadPoolTaskExecutor poolTaskExecutor = getThreadPoolTaskExecutor(Integer.MAX_VALUE, 30, 60, keepAliveSeconds, "Strategy-Batch-Task-");
        poolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        poolTaskExecutor.setAllowCoreThreadTimeOut(true);
        poolTaskExecutor.initialize();
        return poolTaskExecutor;
    }


    /**
     * Strategy thread pool thread pool task executor.
     *
     * @return the thread pool task executor
     */
    @Bean(name = "StrategyThreadPool")
    public ThreadPoolTaskExecutor strategyThreadPool(){
        ThreadPoolTaskExecutor poolTaskExecutor = getThreadPoolTaskExecutor(100000, corePoolSize, maxPoolSize, keepAliveSeconds, "Strategy-Thread-");
        poolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        poolTaskExecutor.setAllowCoreThreadTimeOut(true);
        poolTaskExecutor.initialize();
        return poolTaskExecutor;
    }

    /**
     * Gets queue capacity.
     *
     * @return the queue capacity
     */
    public int getQueueCapacity() {
        return queueCapacity;
    }

    /**
     * Gets core pool size.
     *
     * @return the core pool size
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * Gets max pool size.
     *
     * @return the max pool size
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Gets keep alive seconds.
     *
     * @return the keep alive seconds
     */
    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    /**
     * Gets thread name prefix.
     *
     * @return the thread name prefix
     */
    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

}
