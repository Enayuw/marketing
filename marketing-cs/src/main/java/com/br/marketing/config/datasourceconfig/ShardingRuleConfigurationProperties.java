package com.br.marketing.config.datasourceconfig;

import io.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datasource.config.sharding")
public class ShardingRuleConfigurationProperties extends YamlShardingRuleConfiguration {
}
