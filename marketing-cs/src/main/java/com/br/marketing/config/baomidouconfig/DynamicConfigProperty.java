//package com.br.marketing.config.baomidouconfig;
//
//import com.baomidou.dynamic.datasource.enums.SeataMode;
//import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
//import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
//import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDatasourceAopProperties;
//import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.beecp.BeeCpConfig;
//import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.dbcp2.Dbcp2Config;
//import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig;
//import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.hikari.HikariCpConfig;
//import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
//import com.baomidou.dynamic.datasource.strategy.LoadBalanceDynamicDataSourceStrategy;
//import com.baomidou.dynamic.datasource.toolkit.CryptoUtils;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.context.properties.NestedConfigurationProperty;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//@Slf4j
//@Getter
//@Setter
//@ConfigurationProperties(prefix = "datasource.database")
//public class DynamicConfigProperty {
//
//        public static final String PREFIX = "datasource.database";
//
//        /**
//         * 必须设置默认的库,默认master
//         */
//        private String primary = "master";
//        /**
//         * 是否启用严格模式,默认不启动. 严格模式下未匹配到数据源直接报错, 非严格模式下则使用默认数据源primary所设置的数据源
//         */
//        private Boolean strict = false;
//        /**
//         * 是否使用p6spy输出，默认不输出
//         */
//        private Boolean p6spy = false;
//        /**
//         * 是否使用开启seata，默认不开启
//         */
//        private Boolean seata = false;
//        /**
//         * 是否懒加载数据源
//         */
//        private Boolean lazy = false;
//        /**
//         * seata使用模式，默认AT
//         */
//        private SeataMode seataMode = SeataMode.AT;
//        /**
//         * 全局默认publicKey
//         */
//        private String publicKey = CryptoUtils.DEFAULT_PUBLIC_KEY_STRING;
//        /**
//         * 每一个数据源
//         */
//        private Map<String, DynamicDataSourceProperty> datasource = new LinkedHashMap<>();
//        /**
//         * 多数据源选择算法clazz，默认负载均衡算法
//         */
//        private Class<? extends DynamicDataSourceStrategy> strategy = LoadBalanceDynamicDataSourceStrategy.class;
//        /**
//         * Druid全局参数配置
//         */
//        @NestedConfigurationProperty
//        private DruidConfig druid = new DruidConfig();
//        /**
//         * HikariCp全局参数配置
//         */
//        @NestedConfigurationProperty
//        private HikariCpConfig hikari = new HikariCpConfig();
//        /**
//         * BeeCp全局参数配置
//         */
//        @NestedConfigurationProperty
//        private BeeCpConfig beecp = new BeeCpConfig();
//        /**
//         * DBCP2全局参数配置
//         */
//        @NestedConfigurationProperty
//        private Dbcp2Config dbcp2 = new Dbcp2Config();
//
//        /**
//         * aop with default ds annotation
//         */
//        @NestedConfigurationProperty
//        private DynamicDatasourceAopProperties aop = new DynamicDatasourceAopProperties();
//    }
