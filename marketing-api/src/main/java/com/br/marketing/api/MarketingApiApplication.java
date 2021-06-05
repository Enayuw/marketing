package com.br.marketing.api;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


/**
 * 程序主类
 */
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
public class MarketingApiApplication {

    /**
     * 启动入口
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(MarketingApiApplication.class, args);
    }

}
