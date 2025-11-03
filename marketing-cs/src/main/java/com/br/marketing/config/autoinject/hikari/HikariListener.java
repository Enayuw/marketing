package com.br.marketing.config.autoinject.hikari;

import com.br.cloud.datasource.BrPrometheusHistogramMetricsTrackerFactory;
import com.br.cloud.datasource.DataSourceListener;

import com.br.marketing.config.datasourceconfig.DataSourceAspect;
import com.br.marketing.config.datasourceconfig.DbContextHolder;
import com.br.marketing.config.datasourceconfig.DynamicDataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class HikariListener implements ApplicationListener<ApplicationReadyEvent> {
    private Boolean loaded = false;

    public HikariListener() {
        log.info("=== HikariListener Bean 被创建 ===");
    }

    // 所有需要监控的数据源名称
    private static final List<String> DATA_SOURCE_NAMES = Arrays.asList(
            DataSourceAspect.marketingTikiv,
            DataSourceAspect.marketingTiFlash,
            DataSourceAspect.MARKETING_DORIS,
            DataSourceAspect.MARKETING_BI,
            "shardingmarketing"
    );

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("HikariListener.onApplicationEvent 被调用, loaded={}", loaded);
        if (loaded) {
            log.warn("HikariListener 已经加载过，跳过");
            return;
        }

        ApplicationContext applicationContext = event.getApplicationContext();
        Map<String, DataSource> dataSourceMap = applicationContext.getBeansOfType(DataSource.class);
        log.info("找到 {} 个 DataSource Bean", dataSourceMap.size());
        BrPrometheusHistogramMetricsTrackerFactory metricsFactory = new BrPrometheusHistogramMetricsTrackerFactory();

        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            log.info("检查 DataSource Bean: {}, 类型: {}", entry.getKey(), entry.getValue().getClass().getName());
            DataSource dataSource = entry.getValue();

            // 处理直接的 HikariDataSource
            if (dataSource instanceof HikariDataSource) {
                register(entry.getKey(), (HikariDataSource) dataSource, metricsFactory);
                log.info("注册Hikari监控: {}", entry.getKey());
            }
            // 处理 DynamicDataSource
            else if (dataSource instanceof DynamicDataSource) {
                handleDynamicDataSource(entry.getKey(), (DynamicDataSource) dataSource, metricsFactory);
            }
            // 处理 ShardingDataSource
            else if (dataSource instanceof ShardingDataSource) {
                handleShardingDataSource((ShardingDataSource) dataSource, metricsFactory);
            }
        }

        loaded = true;
        log.info("CustomHikariListener 加载完成");
    }

    /**
     * 处理 DynamicDataSource，遍历内部所有数据源
     */
    private void handleDynamicDataSource(String beanName, DynamicDataSource dynamicDataSource,
                                         BrPrometheusHistogramMetricsTrackerFactory metricsFactory) {
        log.info("发现 DynamicDataSource: {}, 开始注册内部Hikari数据源", beanName);

        for (String dsName : DATA_SOURCE_NAMES) {
            try {
                // 设置上下文切换到指定数据源
                DbContextHolder.setDbType(dsName);

                // 尝试获取 HikariDataSource
                HikariDataSource hikariDs = dynamicDataSource.unwrap(HikariDataSource.class);
                if (hikariDs != null) {
                    register(dsName, hikariDs, metricsFactory);
                    log.info("成功注册 DynamicDataSource 内部 Hikari 监控: {}", dsName);
                }

            } catch (Exception e) {
                log.debug("数据源 {} 不是 HikariDataSource 或不存在: {}", dsName, e.getMessage());
            } finally {
                DbContextHolder.clearDbType();
            }
        }

        // 特殊处理 shardingmarketing（可能包含在 ShardingDataSource 中）
        try {
            DbContextHolder.setDbType("shardingmarketing");
            ShardingDataSource shardingDs = dynamicDataSource.unwrap(ShardingDataSource.class);
            if (shardingDs != null) {
                handleShardingDataSource(shardingDs, metricsFactory);
                log.info("成功注册 DynamicDataSource 内部 ShardingDataSource 监控");
            }
        } catch (Exception e) {
            log.debug("shardingmarketing 不是 ShardingDataSource: {}", e.getMessage());
        } finally {
            DbContextHolder.clearDbType();
        }
    }

    /**
     * 处理 ShardingDataSource，遍历其内部的数据源
     */
    private void handleShardingDataSource(ShardingDataSource shardingDataSource,
                                          BrPrometheusHistogramMetricsTrackerFactory metricsFactory) {
        Map<String, DataSource> innerDataSourceMap = shardingDataSource.getDataSourceMap();
        for (Map.Entry<String, DataSource> innerEntry : innerDataSourceMap.entrySet()) {
            if (innerEntry.getValue() instanceof HikariDataSource) {
                register(innerEntry.getKey(), (HikariDataSource) innerEntry.getValue(), metricsFactory);
                log.info("注册 ShardingDataSource 内部 Hikari 监控: {}", innerEntry.getKey());
            }
        }
    }

    /**
     * 注册 Hikari 监控
     */
    private void register(String poolName, HikariDataSource dataSource,
                          BrPrometheusHistogramMetricsTrackerFactory metricsFactory) {
        try {
            // 设置连接池名称（如果还没有设置）
            dataSource.setPoolName(poolName);
        } catch (IllegalStateException e) {
            log.warn("连接池 {} 名称已设置，跳过重新设置: {}", poolName, e.getMessage());
        }

        // 设置 Prometheus 监控工厂
        dataSource.setMetricsTrackerFactory(metricsFactory);
    }
}