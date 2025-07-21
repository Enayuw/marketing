package com.br.marketing.config;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.SnowflakeIdGenerator;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    /**
     * 2025/7/21 12:48
     * 数据中心id,雪花算法位数限制,目前2个数据中心0、1
     */
    @Value("${snowflake.datacenter.id:-1}")
    private int datacenterId;

    @Value("${snowflake.worker-id:}")
    private Long workerId;

    @Bean
    @ConditionalOnExpression("'${snowflake.datacenter.id}' == '0' or '${snowflake.datacenter.id}' == '1'")
    public SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle() {
        return new SnowflakeRedisGeneratorHandle(redisChgService, applicationName, datacenterId);
    }

    @Bean
    @ConditionalOnMissingBean(SnowflakeRedisGeneratorHandle.class)
    public SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle1() {
        return new SnowflakeRedisGeneratorHandle(null, applicationName, datacenterId);
    }

    /**
     * 2025/7/4 16:38
     * 旧雪花算法，保留
     */
    @Bean
    @Deprecated
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(workerId);
    }
}
