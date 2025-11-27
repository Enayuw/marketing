package com.br.marketing.config.autoinject.hikari;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hikari Prometheus 监控配置类
 */
@Slf4j
@Configuration
public class HikariPrometheusConfiguration {

    public HikariPrometheusConfiguration() {
        log.info("=== HikariPrometheusConfiguration 配置类被加载 ===");
    }

    @Bean
    public HikariListener hikariListener() {
        log.info("=== 注册 HikariListener Bean ===");
        return new HikariListener();
    }
}

