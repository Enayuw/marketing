package com.br.marketing.sync;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.hystrix.EnableHystrixPrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.threadpool.EnablePrometheusIceThreadPool;
import com.br.cloud.web.EnablePrometheusTiming;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(exclude = {MultipartAutoConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@MapperScan("com.br.marketing.mapper")
@ImportResource(locations = {"classpath:scheduler.xml"})
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnableHystrixPrometheus
@EnablePrometheusTiming
@EnableBrCounter(namespace = "marketing_sync")
@EnablePrometheusIceThreadPool
public class SyncApplication {
    public static ConfigurableApplicationContext ac;
    public static void main(String[] args) {
        ac= new SpringApplicationBuilder().sources(SyncApplication.class).run(args);
    }
}
