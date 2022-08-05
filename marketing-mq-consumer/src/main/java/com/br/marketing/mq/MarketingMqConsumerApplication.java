package com.br.marketing.mq;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.hystrix.EnableHystrixPrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.threadpool.EnablePrometheusIceThreadPool;
import com.br.cloud.web.EnablePrometheusTiming;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {MultipartAutoConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@Slf4j
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnableHystrixPrometheus
@EnablePrometheusTiming
@EnableBrCounter(namespace = "marketing_mq_consumer")
@EnablePrometheusIceThreadPool
public class MarketingMqConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketingMqConsumerApplication.class, args);
    }

}
