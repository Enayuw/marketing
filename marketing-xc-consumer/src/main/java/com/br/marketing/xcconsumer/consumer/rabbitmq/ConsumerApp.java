package com.br.marketing.xcconsumer.consumer.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.xiecheng.XieChengActivateDTO;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * rabbitmq 消费端
 */
@Component
public class ConsumerApp {

    private static final Logger log = LoggerFactory.getLogger(ConsumerApp.class);

    @Autowired
    ConsumerService consumerService;

    @Resource
    private XieChengRobDataCollidingService robDataCollidingService;

    /**
     * 消费 携程促活数据接入消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_XIECHENG_COLLIDING_ACTIVATE_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_ACTIVATE)}, containerFactory = "concurrentContainerFactory")
    public void consumerXieChengActivate(Channel channel, Message message) {
        XieChengActivateDTO xieChengActivateDTO = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8),
                new TypeReference<XieChengActivateDTO>() {
                }.getType());
        consumerService.consumerRun(channel, message, robDataCollidingService::activateDataHandle, xieChengActivateDTO, null);
    }

}
