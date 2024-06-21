package com.br.marketing.rule.config;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZkCuratorConfig {

    @Value("${SERVER_LISTS:00}")
    private String serverList;

    @Value("${BASE_SLEEP_TIME_MILLISECONDS:00}")
    private int baseSleepTimeMS;

    @Value("${MAX_RETRIES:00}")
    private int maxRetries;

    /**
     * 初始化客户端
     * @return
     */
    @Bean(initMethod = "start",destroyMethod = "close")
//    @ConditionalOnProperty(name = "SERVER_LISTS")
    public CuratorFramework curatorFramework(){
        // 重连策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMS, maxRetries);
        // 建立客户端
        CuratorFramework client =  CuratorFrameworkFactory.builder()
                .connectString(serverList)
                // 会话超时时间
                .sessionTimeoutMs(60 * 1000)
                // 连接超时时间
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        return client;
    }

}
