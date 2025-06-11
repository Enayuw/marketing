package com.br.marketing.handle;

import com.br.marketing.client.RedisChgService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 带本地缓存的幂等性处理器
 *
 * @author Hua Qiang
 * @date 2025/5/22 14:58
 */
@Component
public class CachedMessageIdempotentHandler extends MessageIdempotentHandler {

    // 本地缓存，存储消息状态信息
    private final Cache<String, MessageCacheInfo> localCache;

    public CachedMessageIdempotentHandler(RedisChgService redisChgService,
                                          SnowflakeRedisGeneratorHandle idGenerator) {
        super(redisChgService, idGenerator);

        // 配置本地缓存
        this.localCache = Caffeine.newBuilder()
                .maximumSize(1000000) // 最多缓存100万条记录
                .expireAfterWrite(5, TimeUnit.MINUTES) // 5分钟后过期
                .build();
    }

    @Override
    public boolean checkAndMarkMessageProcessed(String topic, String messageId) {
        return this.checkAndMarkMessageProcessed(topic, messageId, 60);
    }

    @Override
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, int allowReprocessSeconds) {
        return this.checkAndMarkMessageProcessed(topic, messageId, RECORD_EXPIRE_SECONDS, allowReprocessSeconds);
    }

    @Override
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, long expireSeconds, int allowReprocessSeconds) {
        return this.checkAndMarkMessageProcessed(topic, messageId, expireSeconds,
                MessageProcessStatus.PROCESSING.getValue(), allowReprocessSeconds);
    }

    @Override
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, String value) {
        return this.checkAndMarkMessageProcessed(topic, messageId, RECORD_EXPIRE_SECONDS, value, -1);
    }

    @Override
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, long expireSeconds, String value
            , int allowReprocessSeconds) {
        if (StringUtils.isBlank(messageId)) {
            return true;
        }

        String idempotentKey = buildIdempotentKey(topic, messageId);

        // 1. 先查本地缓存
        MessageCacheInfo cached = localCache.getIfPresent(idempotentKey);
        if (cached != null) {
            if (allowReprocessSeconds > -1) {
                // 需要检查缓存的状态是否已过期
                if (cached.isCompleted()) {
                    return false; // 已完成的消息不需要重新处理
                }
                if (cached.isProcessing()) {
                    // 检查是否为僵尸状态
                    long processingSeconds = (System.currentTimeMillis() - cached.getTimestamp()) / 1000;
                    // 仍在有效处理期内
                    return processingSeconds > allowReprocessSeconds;
                }
            }
        }

        // 2. 本地缓存未命中或需要进一步检查，查询Redis
        boolean result = super.checkAndMarkMessageProcessed(topic, messageId, expireSeconds, value, allowReprocessSeconds);

        // 3. 更新本地缓存
        updateLocalCache(idempotentKey, result, allowReprocessSeconds > -1);

        return result;
    }

    @Override
    public void markMessageCompleted(String topic, String messageId) {
        if (StringUtils.isBlank(messageId)) {
            return;
        }

        // 先调用父类方法更新Redis
        super.markMessageCompleted(topic, messageId);

        // 更新本地缓存为完成状态
        String idempotentKey = buildIdempotentKey(topic, messageId);
        localCache.put(idempotentKey, MessageCacheInfo.completed());
    }

    public void markMessageCompleted(String topic, String messageId, int allowReprocessSeconds) {
        if (allowReprocessSeconds < 0) {
            return;
        }
        markMessageCompleted(topic, messageId);
    }

    @Override
    public void markMessageProcessFailed(String topic, String messageId) {
        markMessageProcessFailed(topic, messageId, 3);
    }

    @Override
    public void markMessageProcessFailed(String topic, String messageId, int maxRetries) {
        if (StringUtils.isBlank(messageId)) {
            return;
        }
        // 清除本地缓存
        String idempotentKey = buildIdempotentKey(topic, messageId);
        localCache.invalidate(idempotentKey);

        // 调用父类方法删除Redis记录
        super.markMessageProcessFailed(topic, messageId, maxRetries);
    }

    @Override
    public void markGroupMessageProcessFailed(String topic, String consumerGroup, String messageId) {
        markGroupMessageProcessFailed(topic, consumerGroup, messageId, 3);
    }

    @Override
    public void markGroupMessageProcessFailed(String topic, String consumerGroup, String messageId, int maxRetries) {
        if (StringUtils.isBlank(messageId)) {
            return;
        }
        // 清除本地缓存
        String idempotentKey = buildGroupIdempotentKey(topic, consumerGroup, messageId);
        localCache.invalidate(idempotentKey);

        // 调用父类方法删除Redis记录
        super.markGroupMessageProcessFailed(topic, consumerGroup, messageId, maxRetries);
    }

    /**
     * 更新本地缓存
     */
    private void updateLocalCache(String idempotentKey, boolean canProcess, boolean hasZombieDetection) {
        if (!canProcess) {
            // 已经被处理或正在处理中
            localCache.put(idempotentKey, MessageCacheInfo.completed());
        } else if (hasZombieDetection) {
            // 开始处理
            localCache.put(idempotentKey, MessageCacheInfo.processing());
        }
    }

    /**
     * 消息缓存信息
     */
    private static class MessageCacheInfo {
        private final boolean isCompleted;
        private final long timestamp;

        private MessageCacheInfo(boolean isCompleted, long timestamp) {
            this.isCompleted = isCompleted;
            this.timestamp = timestamp;
        }

        public static MessageCacheInfo completed() {
            return new MessageCacheInfo(true, System.currentTimeMillis());
        }

        public static MessageCacheInfo processing() {
            return new MessageCacheInfo(false, System.currentTimeMillis());
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public boolean isProcessing() {
            return !isCompleted;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
