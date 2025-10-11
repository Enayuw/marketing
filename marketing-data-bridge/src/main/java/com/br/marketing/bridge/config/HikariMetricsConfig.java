package com.br.marketing.bridge.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Map;

/**
 * Hikari数据源监控配置
 * 用于将Hikari连接池指标暴露给Prometheus监控
 * 
 * 监控指标包括:
 * - hikaricp_connections_active: 当前活跃连接数
 * - hikaricp_connections_idle: 当前空闲连接数
 * - hikaricp_connections_pending: 等待连接的线程数
 * - hikaricp_connections_timeout_total: 连接超时总数
 * - hikaricp_connections_creation_seconds: 连接创建时间
 * - hikaricp_connections_acquire_seconds: 连接获取时间
 * - hikaricp_connections_usage_seconds: 连接使用时间
 * - hikaricp_connections_max: 最大连接数
 * - hikaricp_connections_min: 最小空闲连接数
 */
@Slf4j
@Configuration
@ConditionalOnClass({HikariDataSource.class, MeterRegistry.class})
public class HikariMetricsConfig {

    private final MeterRegistry meterRegistry;
    private final Map<String, DataSource> dataSources;

    public HikariMetricsConfig(MeterRegistry meterRegistry, Map<String, DataSource> dataSources) {
        this.meterRegistry = meterRegistry;
        this.dataSources = dataSources;
    }

    @PostConstruct
    public void bindMetrics() {
        if (dataSources == null || dataSources.isEmpty()) {
            log.warn("No dataSources found for Hikari metrics binding");
            return;
        }

        dataSources.forEach((name, dataSource) -> {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                try {
                    // HikariCP会自动将指标注册到MeterRegistry
                    // 这里只需要设置连接池名称以便区分不同的数据源
                    if (hikariDataSource.getPoolName() == null || hikariDataSource.getPoolName().startsWith("HikariPool-")) {
                        hikariDataSource.setPoolName("marketing-" + name);
                    }
                    log.info("Hikari metrics bound for datasource: {} with pool name: {}", 
                            name, hikariDataSource.getPoolName());
                } catch (Exception e) {
                    log.error("Failed to bind Hikari metrics for datasource: {}", name, e);
                }
            }
        });
    }
}

