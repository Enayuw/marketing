package com.br.marketing.innerapi.rabbitmq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.PushRuleService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    /**
     * 延迟消费 获取推送客服中心数据状态
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = "Marketing_Push_CustomerService_Search", containerFactory = "primaryContainerFactory")
    public void consumerUserStatus(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::getCustomerStatus, o,
                "Marketing.Push.CustomerService.Search.Delay");
    }


    /**
     * 延迟消费 获取推送客服中心数据状态
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = "Marketing_Push_CustomerService", containerFactory = "primaryContainerFactory")
    public void consumerPushUser(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::consumerPushCustomer, o,
                "Marketing.Push.CustomerService.Search.Delay");
    }


    /**
     * 消费 营销平台数据导入异步处理
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = "Marketing_PreUser_Receive", containerFactory = "primaryContainerFactory")
    public void consumerPreUser(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::insertMarketingPreUserSync, o, null);
    }

}
