package com.br.marketing.xc.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.xiecheng.XieChengActivateDTO;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.Impl.xc.XieChengCollidingDataLogService;
import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    @Resource
    private XieChengRobDataCollidingService robDataCollidingService;

    /**
     * 营销携程撞库日志消息消费端
     *
     * @param channel 频道
     * @param message 信息
     * @author senyang.zheng
     * @date 2024/03/22
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_XIECHENG_COLLIDING_LOG_QUEUE, durable = "true"),
        exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true"),
        key = MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG)}, containerFactory = "containerFactory")
    public void consumerMarketingXiechengCollidingLog(Channel channel, Message message) {
        String messageStr = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        List<XieChengCollidingDataLog> collidingDataLogList = JSONArray.parseArray(messageStr, XieChengCollidingDataLog.class);
        consumerService.consumerRun(channel, message, xieChengCollidingDataLogService::saveXieChengCollidingDataLog, collidingDataLogList, null);
    }

    /**
     * 消费 携程促活数据接入消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_XIECHENG_COLLIDING_ACTIVATE_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_ACTIVATE)}, containerFactory = "containerFactory")
    public void consumerXieChengActivate(Channel channel, Message message) {
        XieChengActivateDTO xieChengActivateDTO = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8),
                new TypeReference<XieChengActivateDTO>() {
        }.getType());
        consumerService.consumerRun(channel, message, robDataCollidingService::activateDataHandle, xieChengActivateDTO, null);
    }
}
