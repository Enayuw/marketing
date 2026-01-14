package com.br.marketing.check;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.datasource.EnableDataSourcePrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.web.EnablePrometheusTiming;
import com.br.grpc.utils.BrGrpcUtils;
import com.br.marketing.service.Impl.ConsumerService;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Bairong on 2019/10/30.
 */
@SpringBootApplication(
        exclude = {MultipartAutoConfiguration.class,SpringBootConfiguration.class},
        scanBasePackages = {"com.br.marketing","com.marketingkit"})
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@ImportResource(locations = {"classpath:scheduler.xml"})
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnablePrometheusTiming
@EnableDataSourcePrometheus
@EnableBrCounter(namespace = "marketing_check")
@Slf4j
public class CkeckApplication {
    public static ConfigurableApplicationContext ac;
    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        log.warn("回滚验证日志！");
        log.warn("marketing-check开始启动！");
        ac = SpringApplication.run(CkeckApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                CkeckApplication.stop();
            }
        });
        log.warn("marketing-check启动结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
    }

    /**
     * 对客户端调用不同服务产生的资源连接进行关闭，在项目停止时需要进行关闭
     */
    public static void stop() {
        try {
            ConsumerService.consumerDownStatus = Boolean.TRUE;
            log.warn("消费者下线");
            Thread.sleep(24500L);
            BrGrpcUtils.shutDown();
            log.warn("GRPC服务关闭正常");
        } catch (Exception e) {
            log.error("GRPC服务关闭异常", e);
        }
    }

}
