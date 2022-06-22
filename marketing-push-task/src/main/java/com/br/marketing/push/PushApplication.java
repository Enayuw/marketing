package com.br.marketing.push;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.hystrix.EnableHystrixPrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.threadpool.EnablePrometheusIceThreadPool;
import com.br.cloud.web.EnablePrometheusTiming;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;


/**
 * Created by Bairong on 2019/8/28.
 */
@ImportResource(locations = {"classpath:scheduler.xml"})
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class, SpringBootConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@Slf4j
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnableHystrixPrometheus
@EnablePrometheusTiming
@EnableBrCounter(namespace = "marketing_push_task")
@EnablePrometheusIceThreadPool
public class PushApplication {
    public static ConfigurableApplicationContext ac;

    public static void main(String[] args) {
        Long start=System.currentTimeMillis();
        log.warn("PushApplication开始启动！");
        ac= new SpringApplicationBuilder().sources(PushApplication.class).run(args);
        Long end =System.currentTimeMillis();
        log.warn("PushApplication启动结束，耗时{}",end-start);
    }

}
