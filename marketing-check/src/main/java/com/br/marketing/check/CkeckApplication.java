package com.br.marketing.check;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.hystrix.EnableHystrixPrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.threadpool.EnablePrometheusIceThreadPool;
import com.br.cloud.web.EnablePrometheusTiming;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Bairong on 2019/10/30.
 */
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@ImportResource(locations = {"classpath:scheduler.xml"})
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnableHystrixPrometheus
@EnablePrometheusTiming
@EnableBrCounter(namespace = "marketing_check")
@EnablePrometheusIceThreadPool
public class CkeckApplication {
    public static ConfigurableApplicationContext ac;
    public static void main(String[] args) {
        ac = SpringApplication.run(CkeckApplication.class, args);
    }

}
