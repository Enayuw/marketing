package com.br.marketing.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ServiceConfig {

    @Bean("apipool")
    public ThreadPoolExecutor getApiPool(){
        return new ThreadPoolExecutor(50,200,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(50),new ThreadFactoryBuilder().setNameFormat("requestApi-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean("currentDbpool")
    public ThreadPoolExecutor getcurrentDbpool(){
        return new ThreadPoolExecutor(50,50,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(5000),new ThreadFactoryBuilder().setNameFormat("currentDb-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean("logDbpool")
    public ThreadPoolExecutor getSaveLogDbpool(){
        return new ThreadPoolExecutor(10,20,10L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(5000),new ThreadFactoryBuilder().setNameFormat("logDb-pool-%d").build()
                ,new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
