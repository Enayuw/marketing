package com.br.marketing.fast.task;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {MultipartAutoConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@MapperScan("com.br.marketing.mapper")
@EnableBatchProcessing(modular = true)
@EnableTransactionManagement
@Slf4j
public class FastTaskApplication {

    public static  ApplicationContext ac;

    public static void main(String[] args) {
        ac = SpringApplication.run(FastTaskApplication.class, args);
    }
}
