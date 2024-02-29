package com.br.marketing.origin;

import com.br.marketing.entity.CustomerRoutingKeyConfig;
import com.br.marketing.mapper.CustomerRoutingKeyConfigMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Description CaffeineCache
 * @Author hong.chen
 * @CreateTime 2024/02/28
 */
@Component
@Slf4j
public class CaffeineCache {
    @Resource
    private CustomerRoutingKeyConfigMapper configMapper;

    private static LoadingCache<String, CustomerRoutingKeyConfig> routingKeyCache = null;

    @PostConstruct
    private void init() {
        routingKeyCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(key -> configMapper.getCustomerRoutingKeyConfig(key));
    }


    public static CustomerRoutingKeyConfig getRountingKey(String apiCode){
        return routingKeyCache.get(apiCode);
    }
}
