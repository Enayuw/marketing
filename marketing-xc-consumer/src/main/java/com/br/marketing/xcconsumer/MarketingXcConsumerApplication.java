package com.br.marketing.xcconsumer;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.datasource.EnableDataSourcePrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.web.EnablePrometheusTiming;
import com.br.grpc.utils.BrGrpcUtils;
import com.br.marketing.service.Impl.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(
        exclude = {MultipartAutoConfiguration.class, SpringBootConfiguration.class},
        scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@MapperScan("com.br.marketing.mapper")
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnablePrometheusTiming
@EnableDataSourcePrometheus
@EnableBrCounter(namespace = "marketing_xc_consumer")
@Slf4j
public class MarketingXcConsumerApplication {

    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        log.warn("marketing-xc-consumer开始启动！");
        ConfigurableApplicationContext context = SpringApplication.run(MarketingXcConsumerApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(MarketingXcConsumerApplication::stop));
        log.warn("marketing-xc-consumer启动结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
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
