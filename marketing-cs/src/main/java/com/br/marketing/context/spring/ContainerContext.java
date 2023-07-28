package com.br.marketing.context.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ContainerContext  implements ApplicationContextAware {
    public static ApplicationContext applicationContext;

    public static String cluster;


    public void clusterConfig(@Value("${cluster.flag}") String flag){
        ContainerContext.cluster=flag;
    };

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }
}