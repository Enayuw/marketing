package com.br.marketing.innerapi.consumer.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.strategy.HaloCleanHistoryHandler;
import com.br.marketing.strategy.InterfaceHandlerService;
import com.br.marketing.strategy.UserCenterHandler;
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
    private InterfaceHandlerService interfaceHandlerService;

    @Resource
    private UserCenterHandler userCenterHandler;

    @Autowired
    private HaloCleanHistoryHandler haloCleanHistoryHandler;


    /**
     * 消费 营销平台数据导入异步处理
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_TRANSFER_PUSH_CUSTOMER, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER)}, containerFactory = "primaryContainerFactory")
    public void consumerPreUser(Channel channel, Message message) {
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, pushRuleService::pushPersonalTransferDataWrapper, o, null);
    }




    /**
     * 消费 黑名单
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_TRANSFER_PUSH_BLACK, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_BLACK)}, containerFactory = "primaryContainerFactory")
    public void consumerBlack(Channel channel, Message message) {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, pushRuleService::consumerBlack, msg, null);
    }

    /**
     * 消费 营销平台数据导入异步处理
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_UNIVERSAL_TRANSFER_RECEIVE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE)}, containerFactory = "universalDataContainerFactory")
    public void consumerUniversalTransfer(Channel channel, Message message) {
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, interfaceHandlerService::handleDataDirection, o, MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_ERROR_DELAY);
    }
    /**
     * 携程拨打数据
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_XIECHENG_UNIVERSAL_TRANSFER_RECEIVE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_XIECHENG_UNIVERSAL_TRANSFER_RECEIVE)}, containerFactory = "universalDataContainerFactory")
    public void consumerXieChengUniversalTransfer(Channel channel, Message message) {
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, interfaceHandlerService::handleDataDirection, o,
                MQConstants.ROUTING_KEY_XIECHENG_UNIVERSAL_TRANSFER_RECEIVE);
    }

    /**
     * 延迟消费 获取推送客服中心数据状态
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = "delivery-znyy.notice.queue", containerFactory = "secondaryContainerFactory")
    public void deliveryUserInfo(Channel channel, Message message) {
        String mes = new String(message.getBody(), StandardCharsets.UTF_8);
        log.warn("交付下发用户信息：{}",mes);
        consumerService.consumerRun(channel, message,  userCenterHandler::handleDataUserCenter, mes, null);

    }
    /**
     * 消费 哈啰数据清洗
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_HALUO_CLEAN_HISTORY, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_HALUO_CLEAN_HISTORY)}, containerFactory = "primaryContainerFactory")
    public void haloCleanHistory(Channel channel, Message message) {
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, haloCleanHistoryHandler::haluoCleanHistory, o, null);
    }


}
