package com.br.marketing.handle;

import com.br.marketing.client.RedisChgService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 带本地缓存的幂等性处理器
 * @author Hua Qiang
 * @date 2025/5/22 14:58
 */
@Component
public class CachedMessageIdempotentHandler extends MessageIdempotentHandler {

    // 本地缓存，用于减少Redis访问
    private final Cache<String, Boolean> localCache;

    public CachedMessageIdempotentHandler(RedisChgService redisChgService,
                                          SnowflakeRedisGeneratorHandle idGenerator) {
        super(redisChgService, idGenerator);

        // 配置本地缓存
        this.localCache = Caffeine.newBuilder()
                .maximumSize(10000000) // 最多缓存100万条记录
                .expireAfterWrite(5, TimeUnit.MINUTES) // 10分钟后过期
                .build();
    }

    @Override
    public boolean checkAndMarkMessageProcessed(String topic, String messageId) {
        String idempotentKey = buildIdempotentKey(topic, messageId);

        // 先查本地缓存
        Boolean cached = localCache.getIfPresent(idempotentKey);
        if (cached != null && cached) {
            return false; // 本地缓存命中，表示已处理过
        }

        // 本地缓存未命中，查询Redis
        boolean result = super.checkAndMarkMessageProcessed(topic, messageId);

        // 更新本地缓存
        if (!result) {
            localCache.put(idempotentKey, true);
        }

        return result;
    }
}
