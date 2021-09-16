package com.br.marketing.fast.task.config;



import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.*;

@Configuration
@Slf4j
public class DataSourceConfig {

    private static String DB_NAME = "names";
    private static String DB_DEFAULT_NAME="defaultname";
    private static String DB_DEFAULT_VALUE = "aos.datasource";
    private static final String DATASOURCE_TYPE_DEFAULT = "com.alibaba.druid.pool.DruidDataSource";

    private static final String DB_DRUID="spring.datasource.druid";

    @Autowired
    Environment environment;

    @Autowired
    DataSourceConfigProperty dataSourceConfigProperty;
    /**
     * 读取prop 对 Druid 的数据源设置
     */
    @Component
    @ConfigurationProperties(prefix="datasource.druid")
    protected class DruidDataSourceConfigure{
        private Properties configure;

        public Properties getConfigure() {
            return configure;
        }

        public void setConfigure(Properties configure) {
            this.configure = configure;
        }
    }

    //默认数据源
    DataSource defaultDatSource=null;

    @PostConstruct
    void init(){
        defaultDatSource = buildDataSource(dataSourceConfigProperty.getMarketingWrite());
    }

    @Autowired
    private DruidDataSourceConfigure druidConfigure;

    @Primary
    @Bean
    public DynamicDataSource dynamicDataSource(
            Environment env
    ) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        try
        {
            targetDataSources.put(dataSourceConfigProperty.getMarketingWrite().getName(),defaultDatSource);
        }catch (Exception e) {
            log.info("初始化数据库配置失败", e);
        }
        log.info("初始化数据源");
        DynamicDataSource bean = new DynamicDataSource();
        bean.setTargetDataSources(targetDataSources);
        bean.setDefaultTargetDataSource(defaultDatSource);
        return bean;
    }


    /**
     * 创建数据源
     */
    public DataSource buildDataSource(DataSourceConfigProperty.SourceEntity sourceEntity) {
        try {
            String type = sourceEntity.getType();
            if (type == null) {
                // 默认DataSource
                type = DATASOURCE_TYPE_DEFAULT;
            }
            Map<String,Object> configMap=new Hashtable<>();
            configMap.put("url",sourceEntity.getUrl());
            configMap.put("username",sourceEntity.getUsername());
            configMap.put("password",sourceEntity.getPassword());
            configMap.put("type",type);
            configMap.put("driverClassName",sourceEntity.getDriverClassName());
            if(druidConfigure!=null) {
                Properties druidP = druidConfigure.getConfigure();
                if (druidP != null) {
                    //返回的属性键值对实体
                    Set<Map.Entry<Object, Object>> entrySet = druidP.entrySet();
                    for (Map.Entry<Object, Object> entry : entrySet) {
                        configMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                    }
                }
            }
            return DruidDataSourceFactory.createDataSource(configMap);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.error("buildDataSource:",e);
        }
        catch (Exception e){
            log.error("buildDataSource:",e);
        }
        return null;
    }


    @Bean
    BatchConfigurer configurer() {
        return new DefaultBatchConfigurer(defaultDatSource);
    }

}
