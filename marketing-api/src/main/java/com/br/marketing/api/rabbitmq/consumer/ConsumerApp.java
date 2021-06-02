package com.br.marketing.api.rabbitmq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.PushRuleService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConsumerApp {

    @Autowired
    ConsumerService consumerService;

    @Autowired
    PushRuleService pushRuleService;

    /**
     * 延迟消费 获取推送客服中心数据状态
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "Marketing_Push_CustomerService_Search")
    public void consumerUserStatus(Channel channel, Message message){
        Long o = JSON.parseObject(new String(message.getBody()), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel,message, pushRuleService::getCustomerStatus,o,"Marketing.Push.CustomerService.Search.Delay");
    }

//    @RabbitListener(queues = "Marketing_Push_CustomerService_Search")
    public void consumerRemoveMessage(Channel channel, Message message){
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
