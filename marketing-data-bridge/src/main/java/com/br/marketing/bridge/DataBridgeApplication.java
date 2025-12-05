package com.br.marketing.bridge;

import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.datasource.EnableDataSourcePrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.web.EnablePrometheusTiming;
import com.br.grpc.utils.BrGrpcUtils;
import com.br.marketing.config.autoinject.hikari.EnableHikariPrometheus;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(
    exclude = {
        MultipartAutoConfiguration.class,
        SpringBootConfiguration.class,
        GroovyTemplateAutoConfiguration.class
    },
    scanBasePackages = {"com.br.marketing","com.marketingkit"}
)
@EnableAspectJAutoProxy
@MapperScan("com.br.marketing.mapper")
@ImportResource(locations = {"classpath:scheduler.xml"})
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnablePrometheusTiming
@EnableHikariPrometheus
@EnableBrCounter(namespace = "marketing_data_bridge")
@Slf4j
public class DataBridgeApplication {
    public static ConfigurableApplicationContext ac;

    public static void main(String[] args) {
        log.warn("回滚验证日志！");
        log.warn("marketing-data-bridge开始启动！");
        Long start = System.currentTimeMillis();

        //加入下面配置开启日志功能
        System.setProperty("rocketmq.client.logUseSlf4j", "true");
        //配置客户端日志级别
        System.setProperty("rocketmq.log.level", "WARN");
        //修改日志输入目录  配置  服务YAML配置的APP_HOME环境变量,例如： /opt/SpringCloud
        System.setProperty("rocketmq.log.root", System.getenv("APP_HOME") + "/logs/" + System.getenv("APPNAME") + "/" + System.getenv("POD_NAME"));

        ac = new SpringApplicationBuilder().sources(DataBridgeApplication.class).run(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
        Long end = System.currentTimeMillis();
        log.warn("marketing-data-bridge启动结束，耗时{}s", (end - start) / 1000);
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
