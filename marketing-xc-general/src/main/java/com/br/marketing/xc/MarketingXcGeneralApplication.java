package com.br.marketing.xc;

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

@SpringBootApplication(
        exclude = {MultipartAutoConfiguration.class, SpringBootConfiguration.class},
        scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@MapperScan("com.br.marketing.mapper")
@ImportResource(locations = {"classpath:scheduler.xml"})
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnablePrometheusTiming
@EnableDataSourcePrometheus
@EnableBrCounter(namespace = "marketing_xc_general")
@Slf4j
public class MarketingXcGeneralApplication {
    public static ConfigurableApplicationContext ac;
    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        log.warn("marketing-xc-general开始启动！");
        ac = SpringApplication.run(MarketingXcGeneralApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                MarketingXcGeneralApplication.stop();
            }
        });
        log.warn("marketing-xc-general启动结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
    }
    /**
     * 对客户端调用不同服务产生的资源连接进行关闭，在项目停止时需要进行关闭
     */
    public static void stop() {
        try {
            Thread.sleep(24500L);
            BrGrpcUtils.shutDown();
        }catch (InterruptedException e) {
            log.warn("Interrupted!", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("GRPC服务关闭异常", e);
        }
    }

}
