package com.br.marketing.api;


import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
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
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")
@Slf4j
public class MarketingApiApplication {

    /**
     * 启动入口
     *
     * @param args
     * @return
     */
    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        log.warn("marketing-api开始启动！");
        SpringApplication.run(MarketingApiApplication.class, args);
        log.warn("marketing-api启动结束，耗时{}s", (System.currentTimeMillis() - start) / 1000);
    }

}
