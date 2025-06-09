package com.br.marketing.xcconsumer.config;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class XieChengConsumerConfig {
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Bean(name = "xieChengReportThreadPool")
    public ThreadPoolExecutor xieChengReportThreadPool() {
        Integer XieChengReportThreadPoolNum = marketingCommonConfig.getXieChengReportThreadPoolNum();
        return new ThreadPoolExecutor(
                XieChengReportThreadPoolNum,
                XieChengReportThreadPoolNum + 50,
                10L, TimeUnit.SECONDS
                , new SynchronousQueue<>(), new ThreadFactoryBuilder().setNameFormat("xieCheng-report-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
