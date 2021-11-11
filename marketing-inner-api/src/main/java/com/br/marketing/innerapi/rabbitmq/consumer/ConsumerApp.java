package com.br.marketing.innerapi.rabbitmq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.PushRuleService;
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
     * 消费 营销平台数据导入异步处理
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = MQConstants.MARKETING_TRANSFER_PUSH_CUSTOMER, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER), queues = {MQConstants.MARKETING_TRANSFER_PUSH_CUSTOMER}, containerFactory = "primaryContainerFactory")
    public void consumerPreUser(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, pushRuleService::pushPersonalTransferData, o, null);
    }

}
