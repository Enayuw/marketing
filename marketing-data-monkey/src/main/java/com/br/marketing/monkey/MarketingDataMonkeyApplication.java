package com.br.marketing.monkey;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.hystrix.EnableHystrixPrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.threadpool.EnablePrometheusIceThreadPool;
import com.br.cloud.web.EnablePrometheusTiming;
import com.br.grpc.utils.BrGrpcUtils;
import com.br.monitor.grpc.EnvUtil;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Bairong on 2019/10/30.
 */
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class,SpringBootConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@MapperScan("com.br.marketing.mapper")
@ImportResource(locations = {"classpath:scheduler.xml"})
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnableHystrixPrometheus
@EnablePrometheusTiming
@EnableBrCounter(namespace = "marketing_data_monkey")
@EnablePrometheusIceThreadPool
@Slf4j
public class MarketingDataMonkeyApplication {
    public static ConfigurableApplicationContext ac;
    public static void main(String[] args) {
        ac = SpringApplication.run(MarketingDataMonkeyApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                MarketingDataMonkeyApplication.stop();
            }
        });
    }
    /**
     * 对客户端调用不同服务产生的资源连接进行关闭，在项目停止时需要进行关闭
     */
    public static void stop() {
        try {
            if ("GRPC".equals(EnvUtil.getProperties("GRPC_MODE"))) {
                Thread.sleep(4500L);
                BrGrpcUtils.shutDown();
            }
        } catch (Exception e) {
            log.error("GRPC服务关闭异常", e);
        }
    }
}
