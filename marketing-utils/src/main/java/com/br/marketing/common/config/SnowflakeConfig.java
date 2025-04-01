package com.br.marketing.common.config;

import com.br.marketing.common.utils.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 雪花算法 bean
 *
 * @author Hua Qiang
 * @date 2024-11-13 15:04
 */
@Configuration
public class SnowflakeConfig {

    @Value("${snowflake.worker-id:}")
    private Long workerId;

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(workerId);
    }

}
