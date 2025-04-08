package com.br.marketing.marketingaimqconsumer.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.IPushShuheDataService;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.strategy.InterfaceHandlerService;
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

    @Autowired
    PushRuleService pushRuleService;

    @Autowired
    IPushShuheDataService pushShuheDataService;

    @Autowired
    PushDataService pushDataService;

    @Resource
    private InterfaceHandlerService interfaceHandlerService;

    /**
     * 消费 AI上传数据消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_AI_PREUSER_RECEIVE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE)}, containerFactory = "concurrentContainerFactory")
    public void consumerPreUser(Channel channel, Message message) {
        log.warn("MARKETING_AI_PREUSER_RECEIVE：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::insertMarketingPreUserSync, o,
                MQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_ERROR_DELAY);
    }

    /**
     * 消费 AI推送下游数据消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_AI_UNIVERSAL_RECEIVE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE)}, containerFactory = "concurrentContainerFactory")
    public void consumerUniversalTransfer(Channel channel, Message message) {
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, interfaceHandlerService::handleDataDirection, o,
                MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_ERROR_DELAY);
    }
}
