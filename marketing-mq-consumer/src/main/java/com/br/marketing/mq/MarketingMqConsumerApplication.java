package com.br.marketing.mq;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.hystrix.EnableHystrixPrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.web.EnablePrometheusTiming;
import com.br.grpc.utils.BrGrpcUtils;
import com.br.marketing.service.Impl.ConsumerService;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class, SpringBootConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@Slf4j
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnableHystrixPrometheus
@EnablePrometheusTiming
@EnableBrCounter(namespace = "marketing_mq_consumer")
public class MarketingMqConsumerApplication {

    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        log.warn("marketing-mq-consumer开始启动！");
        SpringApplication.run(MarketingMqConsumerApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                MarketingMqConsumerApplication.stop();
            }
        });
        log.warn("marketing-mq-consumer启动结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
    }


    /**
     * 对客户端调用不同服务产生的资源连接进行关闭，在项目停止时需要进行关闭
     */
    public static void stop() {
        try {
            ConsumerService.consumerDownStatus = Boolean.TRUE;
            log.warn("消费者下线");
            Thread.sleep(4500L);
            BrGrpcUtils.shutDown();
            log.warn("GRPC服务关闭正常");
        } catch (Exception e) {
            log.error("GRPC服务关闭异常", e);
        }
    }
}
