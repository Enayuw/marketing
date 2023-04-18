package com.br.marketing.check.beanhadler;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 在spring托管的bean上下文工具
 *
 * @author Guo Zeqiang
 * @dateTime 2023-04-11 10:06
 */
@Component
public class SpringContextHandler implements ApplicationContextAware {
    public ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return this.applicationContext.getBeansOfType(type);
    }
}
