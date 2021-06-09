package com.br.marketing.api.rabbitmq.consumer;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = "Marketing_Push_CustomerService_Search")
    public void consumerUserStatus(Channel channel, Message message){
        Long o = null;
        try {
            o = JSON.parseObject(new String(message.getBody(),"utf-8"), new TypeReference<Long>() {
            }.getType());
        } catch (UnsupportedEncodingException e) {
            if(log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            e.printStackTrace();
        }
        consumerService.consumerRun(channel,message, pushRuleService::getCustomerStatus,o,"Marketing.Push.CustomerService.Search.Delay");
    }


    /**
     * 延迟消费 获取推送客服中心数据状态
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = "Marketing_PreUser_Receive")
    public void consumerPreUser(Channel channel, Message message){
        Long o = null;
        try {
            o = JSON.parseObject(new String(message.getBody(),"utf-8"), new TypeReference<Long>() {
            }.getType());
        } catch (UnsupportedEncodingException e) {
            if(log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            e.printStackTrace();
        }
        consumerService.consumerRun(channel,message, pushRuleService::insertMarketingPreUserSync,o,null);
    }

    /**
     * 测试消费 堆积消息
     * @param channel 通道
     * @param message 消息体
     */
//    @RabbitListener(queues = "Marketing_Push_CustomerService_Search")
    public void consumerRemoveMessage(Channel channel, Message message){
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            if(log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            e.printStackTrace();
        }
    }
}
