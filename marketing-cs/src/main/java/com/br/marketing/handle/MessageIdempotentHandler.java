package com.br.marketing.handle;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 消息幂等性处理器
 *
 * @author Hua Qiang
 * @date 2025/5/22 14:46
 */
@Component
public class MessageIdempotentHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageIdempotentHandler.class);
    private final RedisChgService redisChgService;
    private final SnowflakeRedisGeneratorHandle idGenerator;
    private final String applicationName;

    // 消息处理记录过期时间(秒)，根据业务特性调整
    private static final long RECORD_EXPIRE_SECONDS = 7200000; // 两个小时

    public MessageIdempotentHandler(RedisChgService redisChgService,
                                    SnowflakeRedisGeneratorHandle idGenerator) {
        this.redisChgService = redisChgService;
        this.idGenerator = idGenerator;
        this.applicationName = idGenerator.getApplicationName();
    }

    /**
     * 生成消息唯一ID - 用于生产者发送消息时设置
     */
    public String generateMessageId() {
        return String.valueOf(idGenerator.nextId());
    }

    /**
     * 处理消息前检查幂等性 - 返回true表示需要处理，返回false表示重复消息
     */
    public boolean checkAndMarkMessageProcessed(String topic, String messageId) {
        return checkAndMarkMessageProcessed(topic, messageId, RECORD_EXPIRE_SECONDS);
    }

    public boolean checkAndMarkMessageProcessed(String topic, String messageId, long expireSeconds) {
        return checkAndMarkMessageProcessed(topic, messageId, expireSeconds, "1");
    }

    public boolean checkAndMarkMessageProcessed(String topic, String messageId, String value) {
        return checkAndMarkMessageProcessed(topic, messageId, RECORD_EXPIRE_SECONDS, value);
    }

    public boolean checkAndMarkMessageProcessed(String topic, String messageId, long expireSeconds, String value) {
        if (StringUtils.isBlank(messageId)) {
            return true;
        }
        String idempotentKey = buildIdempotentKey(topic, messageId);
        // 使用Redis的SETNX原子操作，成功设置表示第一次处理
        return redisChgService.lock(idempotentKey, value, expireSeconds > 0 ? expireSeconds : RECORD_EXPIRE_SECONDS);
    }

    /**
     * 构建幂等性检查的Redis键
     */
    protected String buildIdempotentKey(String topic, String messageId) {
        return RedisKeyConstant.MQ_IDEMPOTENT + "{" + applicationName + "}:" + topic + ":" + messageId;
    }

    /**
     * 构建消费者组特定的幂等性键
     * 适用于同一消息在不同消费者组需要单独处理的场景
     */
    public String buildGroupIdempotentKey(String topic, String consumerGroup, String messageId) {
        return RedisKeyConstant.MQ_IDEMPOTENT + "{" + applicationName + "}:" + topic + ":" + consumerGroup + ":" + messageId;
    }

    /**
     * 标记消息处理失败，允许重新处理
     */
    public void markMessageProcessFailed(String topic, String messageId) {
        markMessageProcessFailed(topic, messageId, 3);
    }

    public void markMessageProcessFailed(String topic, String messageId, int maxRetries) {
        if (StringUtils.isBlank(messageId)) {
            return;
        }
        try {
            String idempotentKey = buildIdempotentKey(topic, messageId);
            retryDelete(idempotentKey, maxRetries);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void markGroupMessageProcessFailed(String topic, String consumerGroup, String messageId) {
        markGroupMessageProcessFailed(topic, consumerGroup, messageId, 3);
    }

    public void markGroupMessageProcessFailed(String topic, String consumerGroup, String messageId, int maxRetries) {
        if (StringUtils.isBlank(messageId)) {
            return;
        }
        try {
            String idempotentKey = buildGroupIdempotentKey(topic, consumerGroup, messageId);
            retryDelete(idempotentKey, maxRetries);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 删除幂等性键，允许重新处理
     */
    private void retryDelete(String idempotentKey, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                long del = redisChgService.del(idempotentKey);
                if (del > 0) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Successfully deleted idempotent key: {}", idempotentKey);
                    }
                    return;
                }
                LOGGER.warn("Delete attempt {} failed for key: {}", i + 1, idempotentKey);
            } catch (Exception e) {
                LOGGER.error("Delete attempt {} failed for key: {}, error: {}",
                        i + 1, idempotentKey, e.getMessage(), e);
                if (i < maxRetries - 1) {
                    try {
                        // 尝试设置过期时间，避免消息不能重复处理
                        redisChgService.expire(idempotentKey, 1);
                    } catch (Exception ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                    try {
                        // 指数退避策略
                        Thread.sleep((long) Math.pow(2, i) * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
}
