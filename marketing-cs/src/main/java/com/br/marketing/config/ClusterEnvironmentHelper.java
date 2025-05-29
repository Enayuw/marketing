package com.br.marketing.config;

import com.br.marketing.common.enums.ClusterEnum;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集群环境辅助配置
 */
@Configuration
@Slf4j
public class ClusterEnvironmentHelper {

    @Bean("clusterEnvironment")
    public String determineClusterEnvironment(@Value("${cluster.flag}") String clusterConfig) {
        String enumName = ClusterEnum.CLUSTER_PROD_C.getName();
        String environment;

        if (StringUtils.isNotBlank(clusterConfig) && enumName.equals(clusterConfig)) {
            environment = "yz";
        } else {
            environment = "zw";
        }
        
        return environment;
    }
}