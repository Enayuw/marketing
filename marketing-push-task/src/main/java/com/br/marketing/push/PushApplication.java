package com.br.marketing.push;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.hystrix.EnableHystrixPrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.web.EnablePrometheusTiming;
import com.br.grpc.utils.BrGrpcUtils;
import com.br.marketing.config.MQConsumerShutdown;
import com.br.marketing.config.autoinject.druid.EnableDruidPrometheus;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
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
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@Slf4j
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnableHystrixPrometheus
@EnablePrometheusTiming
@EnableDruidPrometheus
@EnableBrCounter(namespace = "marketing_push_task")
public class PushApplication {
    public static ConfigurableApplicationContext ac;

    public static void main(String[] args) {
        Long start=System.currentTimeMillis();
        log.warn("PushApplication开始启动！");
        ac= new SpringApplicationBuilder().sources(PushApplication.class).run(args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                PushApplication.stop();
                MQConsumerShutdown bean = ac.getBean(MQConsumerShutdown.class);
                bean.rocketmqDestroy(ac);
            }
        });
        Long end =System.currentTimeMillis();
        log.warn("PushApplication启动结束，耗时{}",end-start);
    }

    /**
     * 对客户端调用不同服务产生的资源连接进行关闭，在项目停止时需要进行关闭
     */
    public static void stop() {
        try {
            Thread.sleep(4500L);
            BrGrpcUtils.shutDown();
        } catch (Exception e) {
            log.error("GRPC服务关闭异常", e);
        }
    }

}
