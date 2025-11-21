package com.br.marketing.rule;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.datasource.EnableDataSourcePrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.web.EnablePrometheusTiming;
import com.br.grpc.utils.BrGrpcUtils;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(
        exclude = {MultipartAutoConfiguration.class, SpringBootConfiguration.class},
        scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.br.marketing.mapper")
@ImportResource(locations = {"classpath:scheduler.xml"})
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnablePrometheusTiming
@EnableDataSourcePrometheus
@EnableBrCounter(namespace = "marketing_rule_scheduler")
@Slf4j
public class RuleSchedulerApplication {
    public static ConfigurableApplicationContext ac;

    public static void main(String[] args) {
        log.warn("回滚验证日志！");
        log.warn("marketing-rule-scheduler开始启动！");
        Long start = System.currentTimeMillis();
        ac = new SpringApplicationBuilder().sources(RuleSchedulerApplication.class).run(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
        Long end = System.currentTimeMillis();
        log.warn("marketing-rule-scheduler启动结束，耗时{}s", (end - start) / 1000);
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
            Thread.currentThread().interrupt();
        }
    }
}
