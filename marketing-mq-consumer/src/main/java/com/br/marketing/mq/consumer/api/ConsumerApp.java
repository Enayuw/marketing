package com.br.marketing.mq.consumer.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.service.IPeriodOfValidityService;
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

    @Resource
    private IPeriodOfValidityService periodOfValidityService;


    /**
     * 消费 营销平台数据导入异步处理
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = MQConstants.MARKETING_PRE_USER_RECEIVE, containerFactory = "fiveDataContainerFactory")
    public void consumerPreUser(Channel channel, Message message) {
        log.warn("MARKETING_PRE_USER_RECEIVE：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::insertMarketingPreUserSync, o, null);
    }

    /**
     * 消费 数禾数据导入异步处理
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener( bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_PRE_USER_SHUHERECEIVE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_PRE_USER_SHUHERECEIVE)}, containerFactory = "fiveDataContainerFactory")
    public void consumerShuHePreUser(Channel channel, Message message) {
        log.warn("MARKETING_PRE_USER_SHUHERECEIVE：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::insertMarketingPreUserSync, o, null);
    }

    /**
     * 消费 转化数据导入异步处理
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = MQConstants.MARKETING_TRANSFER_RECEIVE, containerFactory = "fiveDataContainerFactory")
    public void consumerTransferUser(Channel channel, Message message) {
        log.warn("MARKETING_TRANSFER_RECEIVE：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::consumerTransferData, o, null);
    }

    /**
     * 设置默认有效期范围消费者
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = MQConstants.MARKETING_CONFIG_DEFAULT_VALID_DATE, containerFactory = "fiveDataContainerFactory")
    public void consumerConfigDefaultValidDate(Channel channel, Message message) {
        MarketingSyncUser o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8)
                , new TypeReference<MarketingSyncUser>() {
                }.getType());
        consumerService.consumerRun(channel, message, periodOfValidityService::configValidDateDefault, o, null);
    }
}
