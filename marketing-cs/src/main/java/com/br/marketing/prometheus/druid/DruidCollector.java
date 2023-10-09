package com.br.marketing.prometheus.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.br.marketing.config.datasourceconfig.DataSourceAspect;
import com.br.marketing.config.datasourceconfig.DbContextHolder;
import com.br.marketing.config.datasourceconfig.DynamicDataSource;
import com.br.marketing.context.spring.ContainerContext;
import com.br.marketing.prometheus.druid.DruidMetrics;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.PoolStats;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class DruidCollector extends Collector {


    @Override
    public List<MetricFamilySamples> collect() {
        return collectDruidSource();
    }

    private List<MetricFamilySamples> collectDruidSource() {
        ArrayList<MetricFamilySamples> metricFamilySamples = new ArrayList<>();
        try {
            if(ContainerContext.applicationContext == null){
                return metricFamilySamples;
            }
            Map<String, DataSource> beansOfType = ContainerContext.applicationContext.getBeansOfType(DataSource.class);
            if(beansOfType == null){
                return metricFamilySamples;
            }
            for (Map.Entry<String, DataSource> entry : beansOfType.entrySet()) {

                if (entry.getValue() instanceof DynamicDataSource) {
                    DbContextHolder.setDbType(DataSourceAspect.marketingTikiv);
                    DruidDataSource dsTikv = ((DynamicDataSource) entry.getValue()).unwrap(DruidDataSource.class);
                    metricFamilySamples.addAll(setMetrics(dsTikv,DataSourceAspect.marketingTikiv));
                    DbContextHolder.clearDbType();

                    DbContextHolder.setDbType(DataSourceAspect.marketingTiFlash);
                    DruidDataSource dsFlash = ((DynamicDataSource) entry.getValue()).unwrap(DruidDataSource.class);
                    metricFamilySamples.addAll(setMetrics(dsFlash,DataSourceAspect.marketingTiFlash));
                    DbContextHolder.clearDbType();

                    ShardingDataSource dsSharding = ((DynamicDataSource) entry.getValue()).unwrap(ShardingDataSource.class);
                    for (Map.Entry<String, DataSource> dsShardingDruidSource : dsSharding.getDataSourceMap().entrySet()) {
                        metricFamilySamples.addAll(setMetrics((DruidDataSource) dsShardingDruidSource.getValue(),"shardingmarketing"));
                    }
                }

                if (entry.getValue() instanceof DruidDataSource) {
                    metricFamilySamples.addAll(setMetrics((DruidDataSource) entry.getValue(),"defaultDb"));
                }

                if (entry.getValue() instanceof ShardingDataSource) {
                    Map<String, DataSource> innerDataSourceMap = ((ShardingDataSource) entry.getValue()).getDataSourceMap();
                    for (Map.Entry<String, DataSource> innerEntry : innerDataSourceMap.entrySet()) {
                        if (innerEntry.getValue() instanceof DruidDataSource) {
                            metricFamilySamples.addAll(setMetrics((DruidDataSource) innerEntry.getValue(),innerEntry.getKey()));
                        }
                    }
                }
            }
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
        }
        return metricFamilySamples;
    }

    private List<MetricFamilySamples> setMetrics(DruidDataSource druidDataSource,String dsName){
        ArrayList<MetricFamilySamples> list = new ArrayList<>();
        List<String> labelVaules = Arrays.asList(dsName);
        list.add(createGauge(DruidMetrics.METRIC_NAME_INITIAL_SIZE, "druid_initial_size",labelVaules, (double) druidDataSource.getInitialSize()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_MIN_IDLE, "druid_min_idle",labelVaules, (double) druidDataSource.getMinIdle()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_MAX_ACTIVE, "druid_max_active",labelVaules, (double) druidDataSource.getMaxActive()));

        // connection pool core metrics
        list.add(createGauge(DruidMetrics.METRIC_NAME_ACTIVE_COUNT, "druid_active_count",labelVaules, (double) druidDataSource.getActiveCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_ACTIVE_PEAK, "druid_active_peak",labelVaules, (double) druidDataSource.getActivePeak()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_POOLING_PEAK, "druid_pooling_peak",labelVaules,  (double) druidDataSource.getPoolingPeak()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_POOLING_COUNT, "druid_pooling_count",labelVaules,  (double) druidDataSource.getPoolingCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_WAIT_THREAD_COUNT, "druid_wait_thread_count",labelVaules, (double) druidDataSource.getWaitThreadCount()));

        // connection pool detail metrics
        list.add(createGauge(DruidMetrics.METRIC_NAME_NOT_EMPTY_WAIT_COUNT, "druid_not_empty_wait_count",labelVaules, (double) druidDataSource.getNotEmptyWaitCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_NOT_EMPTY_WAIT_MILLIS, "druid_not_empty_wait_millis",labelVaules, (double) druidDataSource.getNotEmptyWaitMillis()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_NOT_EMPTY_THREAD_COUNT, "druid_not_empty_thread_count",labelVaules,  (double) druidDataSource.getNotEmptyWaitThreadCount()));

        list.add(createGauge(DruidMetrics.METRIC_NAME_LOGIC_CONNECT_COUNT, "druid_logic_connect_count",labelVaules,  (double) druidDataSource.getConnectCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_LOGIC_CLOSE_COUNT, "druid_logic_close_count",labelVaules,  (double) druidDataSource.getCloseCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_LOGIC_CONNECT_ERROR_COUNT, "druid_logic_connect_error_count",labelVaules,  (double) druidDataSource.getConnectErrorCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_PHYSICAL_CONNECT_COUNT, "druid_physical_connect_count",labelVaules, (double) druidDataSource.getCreateCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_PHYSICAL_CLOSE_COUNT, "druid_physical_close_count",labelVaules, (double) druidDataSource.getDestroyCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_PHYSICAL_CONNECT_ERROR_COUNT, "druid_physical_connect_error_count",labelVaules,  (double) druidDataSource.getCreateErrorCount()));

        // sql execution core metrics
        list.add(createGauge(DruidMetrics.METRIC_NAME_ERROR_COUNT, "druid_error_count",labelVaules,  (double) druidDataSource.getErrorCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_EXECUTE_COUNT, "druid_execute_count",labelVaules, (double) druidDataSource.getExecuteCount()));
        // transaction metrics
        list.add(createGauge(DruidMetrics.METRIC_NAME_START_TRANSACTION_COUNT, "druid_start_transaction_count",labelVaules,  (double) druidDataSource.getStartTransactionCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_COMMIT_COUNT, "druid_commit_count",labelVaules,  (double) druidDataSource.getCommitCount()));
        createGauge(DruidMetrics.METRIC_NAME_ROLLBACK_COUNT, "druid_rollback_count",labelVaules,  (double) druidDataSource.getRollbackCount());

        // sql execution detail
        list.add(createGauge(DruidMetrics.METRIC_NAME_PREPARED_STATEMENT_OPEN_COUNT, "druid_prepared_statement_open_count",labelVaules,  (double) druidDataSource.getPreparedStatementCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_PREPARED_STATEMENT_CLOSED_COUNT, "druid_prepared_statement_closed_count",labelVaules,  (double) druidDataSource.getClosedPreparedStatementCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_PSCACHE_ACCESS_COUNT, "druid_ps_cache_access_count",labelVaules,  (double) druidDataSource.getCachedPreparedStatementAccessCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_PSCACHE_HIT_COUNT, "druid_ps_cache_hit_count",labelVaules,  (double) druidDataSource.getCachedPreparedStatementHitCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_PSCACHE_MISS_COUNT, "druid_ps_cache_miss_count",labelVaules,  (double) druidDataSource.getCachedPreparedStatementMissCount()));
//        createGauge(DruidMetrics.METRIC_NAME_EXECUTE_QUERY_COUNT, "druid_execute_query_count",  (double) druidDataSource.getExecuteCount()));
//        createGauge(DruidMetrics.METRIC_NAME_EXECUTE_UPDATE_COUNT, "druid_execute_update_count",  (double) druidDataSource.getExecuteUpdateCount()));
//        createGauge(DruidMetrics.METRIC_NAME_EXECUTE_BATCH_COUNT, "druid_execute_batch_count",  (double) druidDataSource.getExecuteBatchCount()));

        // none core metrics, some are static configurations
        list.add(createGauge(DruidMetrics.METRIC_NAME_MAX_WAIT, "druid_max_wait",labelVaules,  (double) druidDataSource.getMaxWait()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_MAX_WAIT_THREAD_COUNT, "druid_max_wait_thread_count",labelVaules,  (double) druidDataSource.getMaxWaitThreadCount()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_LOGIN_TIMEOUT, "druid_login_timeout",labelVaules,  (double) druidDataSource.getLoginTimeout()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_QUERY_TIMEOUT, "druid_query_timeout",labelVaules,  (double) druidDataSource.getQueryTimeout()));
        list.add(createGauge(DruidMetrics.METRIC_NAME_TRANSACTION_QUERY_TIMEOUT, "druid_transaction_query_timeout",labelVaules, (double) druidDataSource.getTransactionQueryTimeout()));

        return list;
    }



    private GaugeMetricFamily createGauge(String metric, String help,List<String> labels, double value) {
        GaugeMetricFamily metricFamily = new GaugeMetricFamily(metric, help, Arrays.asList("dbName"));
        metricFamily.addMetric(labels,value);
        return metricFamily;
    }
}
