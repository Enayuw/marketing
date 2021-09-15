//package com.br.marketing.fast.task.config;
//
//
//
//import com.alibaba.druid.pool.DruidDataSource;
//import com.alibaba.druid.pool.DruidDataSourceFactory;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.beanutils.BeanMap;
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
//import org.springframework.boot.bind.RelaxedPropertyResolver;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import javax.sql.DataSource;
//import javax.swing.*;
//import java.beans.BeanInfo;
//import java.beans.Introspector;
//import java.beans.PropertyDescriptor;
//import java.sql.SQLException;
//import java.util.*;
//
//@Configuration
//@MapperScan(value ={"com.fang.aos.borgservice.dao"})
//@Slf4j
//public class DataSourceConfig {
//
//    private static String DB_NAME = "names";
//    private static String DB_DEFAULT_NAME="defaultname";
//    private static String DB_DEFAULT_VALUE = "aos.datasource";
//    private static final Object DATASOURCE_TYPE_DEFAULT = "com.alibaba.druid.pool.DruidDataSource";
//
//    private static final String DB_DRUID="spring.datasource.druid";
//
//    @Autowired
//    Environment environment;
//
//    /**
//     * 读取prop 对 Druid 的数据源设置
//     */
//    @Component
//    @ConfigurationProperties(prefix="aos.datasource.druid")
//    protected class DruidDataSourceConfigure{
//        private Properties configure;
//
//        public Properties getConfigure() {
//            return configure;
//        }
//
//        public void setConfigure(Properties configure) {
//            this.configure = configure;
//        }
//    }
//
//    @Autowired
//    private DruidDataSourceConfigure druidConfigure;
//
//    @PostConstruct
//    public void  init(){
//        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, DB_DRUID+".");
//        String filters = propertyResolver.getProperty("filters");
//        if(!StringUtils.isNullOrEmpty(filters))
//        {
//            System.setProperty("druid.filters",filters);
//            log.info("Set System Property: druid.filters:"+filters);
//        }
//    }
//    @Primary
//    @Bean
//    public DynamicDataSource dynamicDataSource(
//            Environment env
//    ) {
//
//        //获取aos.datasource
//        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, DB_DEFAULT_VALUE+".");
//        //获取aos.datasource.names
//        String dsPrefixs = propertyResolver.getProperty(DB_NAME);
//        //获取aos.datasource.defaultname
//        String dbDefaultName= propertyResolver.getProperty(DB_DEFAULT_NAME);
//
//        //默认数据源
//        DataSource defaultDatSource=null;
//
//        Map<Object, Object> targetDataSources = new HashMap<>();
//        try
//        {
//            DbConfig dbConfiguration = DbConfigHelper.getDbConfig(environment.getProperty("datasource.configFile.path"));
//            Map<String, DbInfo> dbInfoMap=new HashMap<>();
//            BeanMap mp = new BeanMap(dbConfiguration);
//            for (Object sd : mp.keySet()) {
//                if (sd.toString().compareToIgnoreCase("class") == 0) {
//                    continue;
//                }
//                dbInfoMap.put(sd.toString(), (DbInfo)mp.get(sd));
//            }
//            for (String dsPrefix : dsPrefixs.split(",")) {
//                Map<String, Object> dsMap = propertyResolver.getSubProperties(dsPrefix + ".");
//                DataSource masterDatSource = buildDataSource(dsMap,dbInfoMap.get(dsPrefix));
//                targetDataSources.put(dsPrefix, masterDatSource);
//                //获取默认数据源
//                if (dbDefaultName.equals(dsPrefix)) {
//                    defaultDatSource = masterDatSource;
//                }
//            }
//        }catch (Exception e) {
//            log.info("初始化数据库配置失败", e);
//        }
//        log.info("初始化数据源");
//        DynamicDataSource bean = new DynamicDataSource();
//        bean.setTargetDataSources(targetDataSources);
//        bean.setDefaultTargetDataSource(defaultDatSource);
//        return bean;
//    }
//
//
//    /**
//     * 创建数据源
//     * @param dsMap
//     * @return
//     */
//    public DataSource buildDataSource(Map<String, Object> dsMap,DbInfo dbInfo) {
//        try {
//            Object type = dsMap.get("type");
//            if (type == null) {
//                // 默认DataSource
//                type = DATASOURCE_TYPE_DEFAULT;
//            }
//
//            //mysql如果需要可以自行设置连接属性urlProperties
//            if("mysql".equals(dbInfo.getDBtype().toLowerCase())){
//                Object urlProperties=dsMap.get("urlProperties");
//                if(urlProperties!=null){
//                    dbInfo.setUrlProperties(urlProperties.toString());
//                }
//            }
//            String url = dbInfo.toString();
//            String username = dbInfo.getDBusername();
//            String password = dbInfo.getDBpw();
//
////            DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url)
////                    .username(username).password(password).type(dataSourceType);
////            DataSource dataSource= factory.build();
//            Map<String,Object> configMap=new Hashtable<>(dsMap);
//            configMap.put("url",url);
//            configMap.put("username",username);
//            configMap.put("password",password);
//            configMap.put("type",type.toString());
//            if(druidConfigure!=null) {
//                Properties druidP = druidConfigure.getConfigure();
//                if (druidP != null) {
//                    //返回的属性键值对实体
//                    Set<Map.Entry<Object, Object>> entrySet = druidP.entrySet();
//                    for (Map.Entry<Object, Object> entry : entrySet) {
//                        configMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
//                    }
//                }
//            }
//            return DruidDataSourceFactory.createDataSource(configMap);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            log.error("buildDataSource:",e);
//        }
//        catch (Exception e){
//            log.error("buildDataSource:",e);
//        }
//        return null;
//    }
//
//
//}
