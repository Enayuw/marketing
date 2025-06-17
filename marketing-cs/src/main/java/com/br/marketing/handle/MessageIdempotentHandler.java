package com.br.marketing.handle;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import lombok.Getter;
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
    static final long RECORD_EXPIRE_SECONDS = 7200; // 两个小时

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
     * 处理消息前检查幂等性
     * 基于是否完成处理的状态值来判断是否需要重新处理
     *
     * @return 返回true表示需要处理，返回false表示重复消息
     */
    public boolean checkAndMarkMessageProcessed(String topic, String messageId) {
        return checkAndMarkMessageProcessed(topic, messageId, RECORD_EXPIRE_SECONDS);
    }

    /**
     * 处理消息前检查幂等性
     *
     * @return 返回true表示需要处理，返回false表示重复消息
     */
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, String value) {
        return checkAndMarkMessageProcessed(topic, messageId, RECORD_EXPIRE_SECONDS, value, -1);
    }

    /**
     * 处理消息前检查幂等性
     * 基于是否完成处理的状态值来判断是否需要重新处理
     *
     * @param allowReprocessSeconds 阈值，超过该值则认为消息处理中已超时，允许重新处理
     * @return 返回true表示需要处理，返回false表示重复消息
     */
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, int allowReprocessSeconds) {
        return checkAndMarkMessageProcessed(topic, messageId, RECORD_EXPIRE_SECONDS, allowReprocessSeconds);
    }

    /**
     * 处理消息前检查幂等性
     * 基于是否完成处理的状态值来判断是否需要重新处理
     *
     * @param expireSeconds 幂等时间，单位秒
     * @return 返回true表示需要处理，返回false表示重复消息
     */
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, long expireSeconds) {
        return checkAndMarkMessageProcessed(topic, messageId, expireSeconds, MessageProcessStatus.PROCESSING.getValue(), 60);
    }

    /**
     * 处理消息前检查幂等性
     *
     * @param expireSeconds 幂等时间，单位秒
     * @param value         自定义缓存值
     * @return 返回true表示需要处理，返回false表示重复消息
     */
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, long expireSeconds, String value) {
        return checkAndMarkMessageProcessed(topic, messageId, expireSeconds, value, -1);
    }

    /**
     * 处理消息前检查幂等性
     * 基于是否完成处理的状态值来判断是否需要重新处理
     *
     * @param expireSeconds         幂等时间，单位秒
     * @param allowReprocessSeconds 阈值，超过该值则认为消息处理中已超时，允许重新处理
     * @return 返回true表示需要处理，返回false表示重复消息
     */
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, long expireSeconds, int allowReprocessSeconds) {
        return checkAndMarkMessageProcessed(topic, messageId, expireSeconds, MessageProcessStatus.PROCESSING.getValue(), allowReprocessSeconds);
    }


    /**
     * 处理消息前检查幂等性
     * 当allowReprocessSeconds>-1时，基于是否完成处理的状态值来判断是否需要重新处理
     *
     * @param expireSeconds         幂等时间，单位秒
     * @param value                 缓存值,当allowReprocessSeconds>-1时，value 为默认值
     * @param allowReprocessSeconds 阈值，超过该值则认为消息处理中已超时，允许重新处理
     * @return 返回true表示需要处理，返回false表示重复消息
     */
    public boolean checkAndMarkMessageProcessed(String topic, String messageId, long expireSeconds, String value, int allowReprocessSeconds) {
        if (StringUtils.isBlank(messageId)) {
            return true;
        }
        String idempotentKey = buildIdempotentKey(topic, messageId);
        try {
            if (allowReprocessSeconds > -1) {
                // 先检查当前状态
                String currentStatus = redisChgService.get(idempotentKey);
                if (StringUtils.isNotBlank(currentStatus)) {
                    String[] parts = currentStatus.split(":");
                    if (parts.length >= 2) {
                        MessageProcessStatus status = MessageProcessStatus.fromValue(parts[0]);
                        if (status != null) {
                            if (status == MessageProcessStatus.COMPLETED) {
                                return false; // 已完成处理，不需要重复处理
                            }
                            if (status == MessageProcessStatus.PROCESSING) {
                                long timestamp = Long.parseLong(parts[1]);
                                // 检查是否为僵尸状态（基于时间戳判断）
                                long processingTime = System.currentTimeMillis() - timestamp;
                                long processingSeconds = processingTime / 1000;
                                if (processingSeconds > (allowReprocessSeconds)) { // 阈值
                                    LOGGER.warn("Detected zombie processing for key: {}, processing time: {}s, allowing retry, thresholdSeconds: {}s",
                                            idempotentKey, processingSeconds, allowReprocessSeconds);
                                    return true;
                                } else {
                                    LOGGER.warn("Message still being processed for key: {}, processing time: {}s, thresholdSeconds: {}s",
                                            idempotentKey, processingSeconds, allowReprocessSeconds);
                                    return false; // 正在处理中，不允许重复处理
                                }
                            }
                        }
                    }
                }
                // 使用带时间戳的状态值
                value = buildStatusValue(MessageProcessStatus.PROCESSING);
            }
            // 使用Redis的SETNX原子操作，成功设置表示第一次处理
            return redisChgService.lock(idempotentKey, value, expireSeconds > 0 ? expireSeconds : RECORD_EXPIRE_SECONDS);
        } catch (Exception e) {
            LOGGER.error("{},topic={}, messageId={}, key={}", e.getMessage(), topic, messageId, idempotentKey, e);
            return true;
        }
    }

    /**
     * 标记消息处理完成
     */
    public void markMessageCompleted(String topic, String messageId) {
        if (StringUtils.isBlank(messageId)) {
            return;
        }
        String idempotentKey = buildIdempotentKey(topic, messageId);
        try {
            // 使用带时间戳的完成状态
            String completedStatusValue = buildStatusValue(MessageProcessStatus.COMPLETED);
            redisChgService.set(idempotentKey, completedStatusValue);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Marked message as completed: topic={}, messageId={}, key={}",
                        topic, messageId, idempotentKey);
            }
        } catch (Exception e) {
            LOGGER.error("{},topic={}, messageId={}, key={}", e.getMessage(), topic, messageId, idempotentKey, e);
        }
    }

    /**
     * 构建带时间戳的状态值
     */
    private String buildStatusValue(MessageProcessStatus status) {
        return status.getValue() + ":" + System.currentTimeMillis();
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
        String idempotentKey = buildIdempotentKey(topic, messageId);
        if (retryDelete(idempotentKey, maxRetries)) {
            LOGGER.error("删除消息幂等失败！topic={}, messageId={},idempotentKey={}, maxRetries={}", topic, messageId, idempotentKey, maxRetries);
        }
    }

    public void markGroupMessageProcessFailed(String topic, String consumerGroup, String messageId) {
        markGroupMessageProcessFailed(topic, consumerGroup, messageId, 3);
    }

    public void markGroupMessageProcessFailed(String topic, String consumerGroup, String messageId, int maxRetries) {
        if (StringUtils.isBlank(messageId)) {
            return;
        }
        String idempotentKey = buildGroupIdempotentKey(topic, consumerGroup, messageId);
        if (retryDelete(idempotentKey, maxRetries)) {
            LOGGER.error("删除消息幂等失败！topic={}, consumerGroup={}, messageId={},idempotentKey={}, maxRetries={}", topic, consumerGroup, messageId, idempotentKey, maxRetries);
        }

    }

    /**
     * 删除幂等性键，允许重新处理
     *
     * @param idempotentKey 幂等性键
     * @param maxRetries    最大重试次数
     * @return true 表示删除失败，false 表示删除成功
     */
    private boolean retryDelete(String idempotentKey, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                long del = redisChgService.del(idempotentKey);
                if (del > 0) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Successfully deleted idempotent key: {}", idempotentKey);
                    }
                    return false;
                }
                LOGGER.warn("Delete attempt {} failed for key: {} (key may not exist)", i + 1, idempotentKey);
                if (redisChgService.exists(idempotentKey)) {
                    // 对于删除失败但无异常的情况，也应该有延迟
                    if (i < maxRetries - 1) {
                        try {
                            Thread.sleep(Math.min((long) Math.pow(2, i) * 100, 3000)); // 限制最大延迟
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } else {
                    return true;
                }
            } catch (Exception e) {
                LOGGER.error("Delete attempt {} failed for key: {}, error: {}",
                        i + 1, idempotentKey, e.getMessage(), e);
                if (i < maxRetries - 1) {
                    try {
                        // 异步删除
                        long l = redisChgService.unlink(idempotentKey);
                        if(l > 0){
                            return false;
                        }
                        LOGGER.warn("unlink attempt {} failed for key: {} fail", i + 1, idempotentKey);
                    } catch (Exception ex) {
                        LOGGER.error("Failed to set expiration for key: {}, error: {}",
                                idempotentKey, ex.getMessage());
                    }
                    try {
                        Thread.sleep(Math.min((long) Math.pow(2, i) * 100, 3000));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return true;
    }

    @Getter
    enum MessageProcessStatus {
        PROCESSING("0"),    // 正在处理中
        COMPLETED("1"),     // 处理完成
        ;

        private final String value;

        MessageProcessStatus(String value) {
            this.value = value;
        }

        public static MessageProcessStatus fromValue(String value) {
            for (MessageProcessStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return null;
        }
    }
}
