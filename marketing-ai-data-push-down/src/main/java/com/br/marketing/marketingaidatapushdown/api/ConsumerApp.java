package com.br.marketing.marketingaidatapushdown.api;

import com.br.marketing.common.enums.SwitchMessageQueueEnum;
import com.br.marketing.common.utils.AiMQConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.IPushShuheDataService;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.Impl.ai.AiConsumerService;
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

import static com.br.marketing.common.utils.AiMQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_ERROR_RETRY;


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

    @Autowired
    AiConsumerService aiConsumerService;

    /**
     * 消费 AI推送下游数据消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = AiMQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE)}, containerFactory = "concurrentContainerFactory")
    public void consumerUniversalTransfer(Channel channel, Message message) {
        log.warn("MARKETING_AI_UNIVERSAL_RECEIVE：获取消息成功");
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        aiConsumerService.consumerAndCacheMsgCount(channel, message, interfaceHandlerService::handleDataDirection, o,
                ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_ERROR_RETRY, SwitchMessageQueueEnum.MARKETING_AI_UNIVERSAL_RECEIVE.getQueueType());
    }

    /**
     * 消费 AI推送下游数据消费端-备用1
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_1, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = AiMQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_1)}, containerFactory = "concurrentContainerFactory")
    public void consumerUniversalTransfer1(Channel channel, Message message) {
        log.warn("MARKETING_AI_UNIVERSAL_RECEIVE_1：获取消息成功");
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        aiConsumerService.consumerAndCacheMsgCount(channel, message, interfaceHandlerService::handleDataDirection, o,
                ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_ERROR_RETRY, SwitchMessageQueueEnum.MARKETING_AI_UNIVERSAL_RECEIVE.getQueueType());
    }

    /**
     * 消费 AI推送下游数据消费端-备用2
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_2, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = AiMQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_2)}, containerFactory = "concurrentContainerFactory")
    public void consumerUniversalTransfer2(Channel channel, Message message) {
        log.warn("MARKETING_AI_UNIVERSAL_RECEIVE_2：获取消息成功");
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        aiConsumerService.consumerAndCacheMsgCount(channel, message, interfaceHandlerService::handleDataDirection, o,
                ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_ERROR_RETRY, SwitchMessageQueueEnum.MARKETING_AI_UNIVERSAL_RECEIVE.getQueueType());
    }

    /**
     * 消费 AI推送下游异常数据重试消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_ERROR_RETRY, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = AiMQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_ERROR_RETRY)}, containerFactory = "concurrentContainerFactory")
    public void consumerUniversalTransferErrorDelay(Channel channel, Message message) {
        log.warn("MARKETING_AI_UNIVERSAL_RECEIVE_ERROR_RETRY：获取消息成功");
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        aiConsumerService.consumerErrorRetry(channel, message, interfaceHandlerService::handleDataDirection, o);
    }
}
