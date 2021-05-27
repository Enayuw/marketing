package com.br.marketing.config;

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
        return new ThreadPoolExecutor(50,200,10L, TimeUnit.SECONDS, new ArrayBlockingQueue(50),new ThreadPoolExecutor.AbortPolicy());
    }
}
