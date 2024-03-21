package com.br.marketing.xc.consumer;

import javax.annotation.Resource;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.Impl.xc.XieChengCollidingDataLogService;
import com.rabbitmq.client.Channel;

/**
 * rabbitmq 消费端
 *
 * @author senyang.zheng
 * @date 2024/03/21
 */
@Component
public class ConsumerApp {

    @Resource
    private ConsumerService consumerService;

    @Resource
    private XieChengCollidingDataLogService xieChengCollidingDataLogService;

    /**
     * 消费 营销平台数据导入异步处理
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_XIECHENG_COLLIDING_LOG_QUEUE, durable = "true"),
        exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true"),
        key = MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG)}, containerFactory = "containerFactory")
    public void consumerUniversalTransfer(Channel channel, Message message) {
        XieChengCollidingDataLog xieChengCollidingDataLog = JSONObject.parseObject(message.getBody(), XieChengCollidingDataLog.class);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, xieChengCollidingDataLogService::saveXieChengCollidingDataLog, xieChengCollidingDataLog,
            MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_ERROR_DELAY);
    }

}
