package com.br.marketing.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext ac;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ac = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        T bean = ac.getBean(clazz);
        return bean;
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        T bean  = ac.getBean(beanName, clazz);
        return bean;
    }
}