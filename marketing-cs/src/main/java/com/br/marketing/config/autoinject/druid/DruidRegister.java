package com.br.marketing.config.autoinject.druid;


import com.br.marketing.prometheus.druid.DruidCollector;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

@SuppressWarnings("all")
public class DruidRegister implements ImportBeanDefinitionRegistrar {

    private static boolean initialized = false;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        synchronized (DruidRegister.class) {
            if (!initialized) {
                new DruidCollector().register();
            }
            initialized = true;
        }
    }
}
