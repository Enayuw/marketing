package com.br.marketing.marketingaidatapushdown;

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
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(
        exclude = {MultipartAutoConfiguration.class, SpringBootConfiguration.class, GroovyTemplateAutoConfiguration.class},
        scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@Slf4j
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnablePrometheusTiming
@EnableDataSourcePrometheus
@EnableBrCounter(namespace = "marketing_ai_data_push_down")
public class MarketingAiDataPushDownApplication {

    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        log.warn("marketing-ai-data-push-down开始启动！");

        //加入下面配置开启日志功能
        System.setProperty("rocketmq.client.logUseSlf4j", "true");
        //配置客户端日志级别
        System.setProperty("rocketmq.log.level", "WARN");
        //修改日志输入目录  配置  服务YAML配置的APP_HOME环境变量,例如： /opt/SpringCloud
        System.setProperty("rocketmq.log.root", System.getenv("APP_HOME") + "/logs/" + System.getenv("APPNAME") + "/" + System.getenv("POD_NAME"));

        ConfigurableApplicationContext context = SpringApplication.run(MarketingAiDataPushDownApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(MarketingAiDataPushDownApplication::stop));
        log.warn("marketing-ai-data-push-down启动结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
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
