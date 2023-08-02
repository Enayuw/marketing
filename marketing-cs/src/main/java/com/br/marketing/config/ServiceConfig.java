package com.br.marketing.config;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ServiceConfig {

    @Bean(name = "apipool")
    public ThreadPoolExecutor getApiPool(){
        return new ThreadPoolExecutor(50,200,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(50),new ThreadFactoryBuilder().setNameFormat("requestApi-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(name = "currentDbpool")
    public ThreadPoolExecutor getcurrentDbpool(){
        return new ThreadPoolExecutor(5,5,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(5000),new ThreadFactoryBuilder().setNameFormat("currentDb-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(name = "logDbpool")
    public ThreadPoolExecutor getSaveLogDbpool(){
        return new ThreadPoolExecutor(10,20,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(5000),new ThreadFactoryBuilder().setNameFormat("logDb-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }


    @Bean(name = "interfaceLogDbpool")
    public ThreadPoolExecutor getInterfaceLogDbpool(){
        return new ThreadPoolExecutor(10,20,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(5000),new ThreadFactoryBuilder().setNameFormat("interfaceLogDb-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Bean(name = "xieChengThreadPool")
    public ThreadPoolExecutor xieChengThreadPool() {
        Integer xiechengDataSendThread = marketingCommonConfig.getXiechengDataSendThread();
        return new ThreadPoolExecutor(xiechengDataSendThread,xiechengDataSendThread,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(5000),new ThreadFactoryBuilder().setNameFormat("xieCheng-pushData-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }
    @Bean(name = "xieChengSmsThreadPool")
    public ThreadPoolExecutor xieChengSmsThreadPool() {
        Integer threadNum = marketingCommonConfig.getXieChengSmsMqPushCustomerThreadNum();
        return new ThreadPoolExecutor(threadNum,threadNum,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(5000),new ThreadFactoryBuilder().setNameFormat("xieCheng-sms-pushCustomer-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(name = "yiXinExcludeRuleFifthThreadPool")
    public ThreadPoolExecutor yiXinExcludeRuleFifthThreadPool() {
        Integer threadNum = marketingCommonConfig.getYiXinExcludeRuleFifthThreadNum();
        return new ThreadPoolExecutor(threadNum,threadNum,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(5000),new ThreadFactoryBuilder().setNameFormat("yiXinTojueCe-ruleFifth-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
