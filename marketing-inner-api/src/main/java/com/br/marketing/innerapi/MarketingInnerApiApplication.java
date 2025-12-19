package com.br.marketing.innerapi;


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

/**
 * 程序主类
 *
 * @Author linquan.guo
 * @CreateDate 2021/8/2 14:32
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/8/2 14:32
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@SpringBootApplication(
        exclude = {MultipartAutoConfiguration.class, SpringBootConfiguration.class, GroovyTemplateAutoConfiguration.class},
        scanBasePackages = {"com.br.marketing", "com.marketingkit"})
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@Slf4j
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnablePrometheusTiming
@EnableDataSourcePrometheus
@EnableBrCounter(namespace = "marketing_inner_api")
public class MarketingInnerApiApplication {


    public static ConfigurableApplicationContext ac;

    /**
     * 启动入口
     *
     * @param args
     * @return
     */
    public static void main(String[] args) {
        Long start = System.currentTimeMillis();

        log.warn("marketing-inner-api开始启动！");
        ac = SpringApplication.run(MarketingInnerApiApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                MarketingInnerApiApplication.stop();
            }
        });
        log.warn("marketing-inner-api启动结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
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
