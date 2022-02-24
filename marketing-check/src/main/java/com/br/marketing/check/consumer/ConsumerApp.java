package com.br.marketing.check.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.PushDataService;
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
    PushDataService pushDataService;

    @Autowired
    PushRuleService pushRuleService;

    /**
     * 延迟消费 获取推送客服中心数据状态
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = MQConstants.MARKETING_PUSH_DASS_SCORE, containerFactory = "containerFactory")
    public void consumerPushDass(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushDataService::pushDassData, o,"");
    }

    /**
     * 消费 黑名单
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_PUSH_BLACK, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_PUSH_BLACK)}, containerFactory = "containerFactory")
    public void consumerCommonBlack(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, pushRuleService::consumerCommonBlack, o, null);
    }

    /**
     * 消费七七转化数据
     * @param channel
     * @param message
     */
    @RabbitListener(queues = MQConstants.MARKETING_PUSH_TWOSEVEN_FILETRANSFER, containerFactory = "containerFactory")
    public void consumerPushTwoSeven(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushDataService::pushSevenTransferData, o,"");
    }
}
