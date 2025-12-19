package com.br.marketing.monkey;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.datasource.EnableDataSourcePrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.web.EnablePrometheusTiming;
import com.br.grpc.utils.BrGrpcUtils;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Bairong on 2019/10/30.
 */
@SpringBootApplication(
        exclude = {MultipartAutoConfiguration.class,SpringBootConfiguration.class},
        scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.br.marketing.mapper")
@ImportResource(locations = {"classpath:scheduler.xml"})
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnablePrometheusTiming
@EnableDataSourcePrometheus
@EnableBrCounter(namespace = "marketing_data_monkey")
@Slf4j
public class MarketingDataMonkeyApplication {
    public static ConfigurableApplicationContext ac;
    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        log.warn("marketing-data-monkey开始启动！");
        ac = SpringApplication.run(MarketingDataMonkeyApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                MarketingDataMonkeyApplication.stop();
            }
        });
        log.warn("marketing-data-monkey启动结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
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
