package com.br.marketing.innerapi.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 营销埋点Kafka消费者 - 测试用，后续删除
 * 
 * @Author system
 * @Date 2024/12/19
 */
@Component
@Slf4j
public class MarketingTrackKafkaConsumer {

    /**
     * 消费marketing-sys-track topic的消息
     * 
     * @param message 消息内容
     * @param partition 分区
     * @param offset 偏移量
     */
    @KafkaListener(topics = "marketing-sys-track")
    public void consumeMarketingTrackMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            log.warn("=== Kafka消费开始 ===");
            log.warn("Topic: marketing-sys-track");
            log.warn("Partition: {}", partition);
            log.warn("Offset: {}", offset);
            log.warn("Message Length: {}", message != null ? message.length() : 0);
            log.warn("=== 完整消息体 ===");
            log.warn("{}", message);
            log.warn("=== 消息体结束 ===");
            
            // 解析消息内容
            if (message != null && !message.trim().isEmpty()) {
                try {
                    JSONObject jsonMessage = JSON.parseObject(message);
                    log.warn("解析后的消息内容: {}", jsonMessage.toJSONString());
                    
                    // 提取关键信息
                    String apiCode = jsonMessage.getString("apiCode");
                    String event = jsonMessage.getString("event");
                    String syncInfoId = jsonMessage.getString("syncInfoId");
                    
                    log.warn("关键信息 - apiCode: {}, event: {}, syncInfoId: {}", 
                            apiCode, event, syncInfoId);
                    
                } catch (Exception e) {
                    log.warn("消息解析失败，原始消息: {}", message);
                    log.warn("解析异常: {}", e.getMessage());
                }
            }
            
            // 自动确认消息（配置中已设置enable-auto-commit: true）
            log.warn("消息处理完成，自动确认");
            
            log.warn("=== Kafka消费成功 ===");
            
        } catch (Exception e) {
            log.error("=== Kafka消费失败 ===");
            log.error("Topic: marketing-sys-track");
            log.error("Partition: {}", partition);
            log.error("Offset: {}", offset);
            log.error("Message Length: {}", message != null ? message.length() : 0);
            log.error("=== 完整消息体（异常时） ===");
            log.error("{}", message);
            log.error("=== 消息体结束 ===");
            log.error("消费异常: {}", e.getMessage(), e);
            
            // 消费失败时自动确认消息（配置中已设置enable-auto-commit: true）
            log.warn("消费失败但已自动确认消息");
        }
    }
}
