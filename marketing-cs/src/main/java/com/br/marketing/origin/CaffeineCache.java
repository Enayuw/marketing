package com.br.marketing.origin;

import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.entity.CustomerRoutingKeyConfig;
import com.br.marketing.service.CustomerRoutingKeyConfigService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @Description CaffeineCache
 * @Author hong.chen
 * @CreateTime 2024/02/28
 */
@Component
@Slf4j
public class CaffeineCache {
    @Autowired
    private CustomerRoutingKeyConfigService configService;

    private LoadingCache<String, CustomerRoutingKeyConfig> routingKeyCache = null;

    // 添加一个新的缓存用于存储标识
    private Cache<String, String> identifierCache = null;

    //添加缓存用于存储mock开关状态
    private Cache<String, MockInitDTO> mockLocalCache = null;

    @PostConstruct
    private void init() {
        routingKeyCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(key -> configService.getCustomerRoutingKeyConfig(key));

        // 初始化标识缓存
        identifierCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        //初始化mock开关状态
        mockLocalCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(7, TimeUnit.DAYS)
                .build();

    }


    public CustomerRoutingKeyConfig getRountingKey(String apiCodeJointBizType) {
        return routingKeyCache.get(apiCodeJointBizType);
    }


    /**
     * 存储一个标识
     * @param identifier 标识键
     * @param value
     */
    public void storeIdentifier(String identifier, String value) {
        identifierCache.put(identifier, value);
    }

    /**
     * 判断标识是否存在
     * @param identifier 标识键
     * @return 如果标识存在则返回true，否则返回false
     */
    public boolean hasIdentifier(String identifier) {
        String value = identifierCache.getIfPresent(identifier);
        return value != null;
    }

    /**
     * 存储一个mock开关状态
     * @param key   唯一key
     * @param value 开关状态
     */
    public void storeMockSwitchStatus(String key, MockInitDTO value) {
        mockLocalCache.put(key,value);
    }

    /**
     * 获取mock开关状态
     * @param key 唯一key
     * @return mock开关状态，如果不存在则返回null
     */
    public MockInitDTO getMockSwitchStatus(String key) {
        return mockLocalCache.getIfPresent(key);
    }

    /**
     * 删除mock开关状态
     * @param key 唯一key
     */
    public void deleteMockSwitchStatus(String key) {
        mockLocalCache.invalidate(key);
    }

    public Cache<String, MockInitDTO> getAllMockLocalCache() {
        return mockLocalCache;
    }

}
