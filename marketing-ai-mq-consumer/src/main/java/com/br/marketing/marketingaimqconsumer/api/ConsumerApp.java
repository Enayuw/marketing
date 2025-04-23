package com.br.marketing.marketingaimqconsumer.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.enums.SwitchMessageQueueEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.AiMQConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.IPushShuheDataService;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.Impl.ai.AiConsumerService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.PushRuleService;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
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

import static com.br.marketing.common.utils.AiMQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_ERROR_RETRY;

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

    @Autowired
    AiConsumerService aiConsumerService;

    TpDynamicExecutor aiConsumerPreUserThreadPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.AI_PREUSER_RECEIVE.getName(), 50, 50, 1);
    TpDynamicExecutor aiConsumerPreUserThreadPool_1 = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.AI_PREUSER_RECEIVE_1.getName(), 50, 50, 1);
    TpDynamicExecutor aiConsumerPreUserThreadPool_2 = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.AI_PREUSER_RECEIVE_2.getName(), 50, 50, 1);

    /**
     * 消费 AI上传数据消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = AiMQConstants.MARKETING_AI_PREUSER_RECEIVE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = AiMQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE)}, containerFactory = "concurrentContainerFactory")
    public void consumerPreUser(Channel channel, Message message) {
        log.warn("MARKETING_AI_PREUSER_RECEIVE：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        aiConsumerService.consumer(channel, message, pushRuleService::insertMarketingPreUserSync, o,
                ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_ERROR_RETRY,
                aiConsumerPreUserThreadPool);
    }

    /**
     * 消费 AI上传数据消费端-备用1
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_1, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = AiMQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_1)}, containerFactory = "concurrentContainerFactory")
    public void consumerPreUser1(Channel channel, Message message) {
        log.warn("MARKETING_AI_PREUSER_RECEIVE_1：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        aiConsumerService.consumer(channel, message, pushRuleService::insertMarketingPreUserSync, o,
                ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_ERROR_RETRY,
                aiConsumerPreUserThreadPool_1);
    }

    /**
     * 消费 AI上传数据消费端-备用2
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_2, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = AiMQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_2)}, containerFactory = "concurrentContainerFactory")
    public void consumerPreUser2(Channel channel, Message message) {
        log.warn("MARKETING_AI_PREUSER_RECEIVE_2：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        aiConsumerService.consumer(channel, message, pushRuleService::insertMarketingPreUserSync, o,
                ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_ERROR_RETRY,
                aiConsumerPreUserThreadPool_2);
    }

    /**
     * 消费 AI上传数据异常重试消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_ERROR_RETRY, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_ERROR_RETRY)}, containerFactory = "concurrentContainerFactory")
    public void consumerUniversalTransferErrorRetry(Channel channel, Message message) {
        log.warn("MARKETING_AI_PREUSER_RECEIVE_ERROR_RETRY：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, pushRuleService::insertMarketingPreUserSync, o, null);
    }
}
