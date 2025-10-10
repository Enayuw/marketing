package com.br.marketing.innerapi.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
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
     * @param acknowledgment 确认机制
     * @param partition 分区
     * @param offset 偏移量
     */
    @KafkaListener(topics = "marketing-sys-track")
    public void consumeMarketingTrackMessage(
            @Payload String message,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            log.warn("=== Kafka消费开始 ===");
            log.warn("Topic: marketing-sys-track");
            log.warn("Partition: {}", partition);
            log.warn("Offset: {}", offset);
            log.warn("Message: {}", message);
            
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
            
            // 手动确认消息
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.warn("消息确认成功");
            }
            
            log.warn("=== Kafka消费成功 ===");
            
        } catch (Exception e) {
            log.error("=== Kafka消费失败 ===");
            log.error("Topic: marketing-sys-track");
            log.error("Partition: {}", partition);
            log.error("Offset: {}", offset);
            log.error("Message: {}", message);
            log.error("消费异常: {}", e.getMessage(), e);
            
            // 消费失败时可以选择不确认，让消息重新消费
            // 这里为了测试，仍然确认消息
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.warn("消费失败但已确认消息");
            }
        }
    }
}
