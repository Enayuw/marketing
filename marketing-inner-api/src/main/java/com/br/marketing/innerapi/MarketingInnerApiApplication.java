package com.br.marketing.innerapi;


import com.br.cloud.boot.EnablePrometheusEndpoint;
import com.br.cloud.counter.EnableBrCounter;
import com.br.cloud.hystrix.EnableHystrixPrometheus;
import com.br.cloud.jvm.EnablePrometheusJvm;
import com.br.cloud.threadpool.EnablePrometheusIceThreadPool;
import com.br.cloud.web.EnablePrometheusTiming;
import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;


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
@ImportResource(locations = {"classpath:scheduler.xml"})
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class, SpringBootConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@Slf4j
@EnablePrometheusEndpoint
@EnablePrometheusJvm
@EnableHystrixPrometheus
@EnablePrometheusTiming
@EnableBrCounter(namespace = "marketing_inner_api")
@EnablePrometheusIceThreadPool
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
        ac =SpringApplication.run(MarketingInnerApiApplication.class, args);
        log.warn("marketing-inner-api启动结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
    }

}
