package com.br.marketing.config;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;

/**
 * 雪花算法配置
 *
 * @author Hua Qiang
 * @date 2025/5/22 14:51
 */
@Configuration
public class SnowflakeConfig {

    @Resource
    private RedisChgService redisChgService;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${snowflake.datacenter.id:0}")
    private long datacenterId;

    // 配置改进版雪花算法（摆脱时钟依赖）
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle() {
        return new SnowflakeRedisGeneratorHandle(redisChgService, applicationName, datacenterId);
    }
}
