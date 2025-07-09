package com.br.marketing.config;

import com.br.marketing.common.utils.SnowflakeIdGenerator;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author Hua Qiang
 * @date 2025/6/9 20:16
 */
@Configuration
public class SnowflakeConfig {


    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${snowflake.datacenter.id:0}")
    private long datacenterId;

    // 配置改进版雪花算法（摆脱时钟依赖）
    @Bean
    @Order()
    public SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle() {
        return new SnowflakeRedisGeneratorHandle(null, applicationName, datacenterId);
    }


    /**
     * 2025/7/4 16:38
     * 旧雪花算法，保留
     */
    @Bean
    @Order()
    @Deprecated
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(datacenterId);
    }
}
