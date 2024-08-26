package com.br.marketing.task;


import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.hystrix.EnableHystrixPrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.web.EnablePrometheusTiming;
import com.br.grpc.utils.BrGrpcUtils;
import com.br.marketing.config.autoinject.druid.EnableDruidPrometheus;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@ImportResource(locations = {"classpath:scheduler.xml"})
@SpringBootApplication(exclude = {SpringBootConfiguration.class},scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")

@Slf4j
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnableHystrixPrometheus
@EnablePrometheusTiming
@EnableDruidPrometheus
@EnableBrCounter(namespace = "marketing_task")
public class Scheduler {
    public static ConfigurableApplicationContext ac;
    /**
     * @param args incr 增量、all 全量、once 一次
     */
    public static void main(String[] args) {
        log.warn("回滚验证日志！");
        Long start=System.currentTimeMillis();
        log.warn("Scheduler开始启动！");
        ac= new SpringApplicationBuilder().sources(Scheduler.class).run(args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Scheduler.stop();
            }
        });
        Long end =System.currentTimeMillis();
        log.warn("Scheduler启动结束，耗时{}",end-start);
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
