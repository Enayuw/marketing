package com.br.marketing.config.datasourceconfig;


import com.google.common.base.Preconditions;
import io.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import io.shardingsphere.shardingjdbc.spring.boot.util.PropertyUtil;
import io.shardingsphere.shardingjdbc.util.DataSourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
@EnableConfigurationProperties({ShardingRuleConfigurationProperties.class})
public class DataSourceConfig {

    static String prefix = "datasource.";

    @Autowired
    private ShardingRuleConfigurationProperties shardingProperties;

    @Autowired
    Environment environment;

    @Primary
    @Bean
    @ConditionalOnProperty(prefix = "datasource.database",name = "defaultSource",havingValue = "shardingmarketing",matchIfMissing = false)
    public DynamicDataSource dynamicDataSource() {
        String defaultName = environment.getProperty(prefix.concat("database.defaultSource"));
        Map<Object, Object> targetDataSources = new HashMap<>();
        try {
            targetDataSources = buildSource();
        } catch (Exception e) {
            log.error("初始化数据库配置失败", e);
        }
        log.info("初始化数据源");
        DynamicDataSource bean = new DynamicDataSource();
        bean.setTargetDataSources(targetDataSources);
        bean.setDefaultTargetDataSource(targetDataSources.get(defaultName));
        return bean;
    }


    public HashMap<Object, Object> buildSource() throws ReflectiveOperationException, SQLException {
        HashMap dataSourceMap = new HashMap();
        String prefixName = prefix.concat("database.");
        String _nameOfBootDbs = environment.getProperty(prefixName + "names");
        for (String _nameOfBootDb : _nameOfBootDbs.split(",")) {
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefixName + _nameOfBootDb, Map.class);
            Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource properties!");
            DataSource dataSource = DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
            if (dataSourceProps.get("isSharding") != null && dataSourceProps.get("isSharding").toString().equals("true")) {
                HashMap _dataSourceMap = new HashMap();
                _dataSourceMap.put(_nameOfBootDb, dataSource);
                dataSourceMap.put(_nameOfBootDb, shardingSourceExtra(_dataSourceMap));
            } else {
                dataSourceMap.put(_nameOfBootDb, dataSource);
            }

        }
        return dataSourceMap;
    }

    public DataSource shardingSourceExtra(Map<String, DataSource> dataSourceMap) throws SQLException {
        return ShardingDataSourceFactory.createDataSource(dataSourceMap
                , shardingProperties.getShardingRuleConfiguration()
                , shardingProperties.getConfigMap()
                , shardingProperties.getProps());
    }


}
