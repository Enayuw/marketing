package com.br.marketing.check.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.XieChengSmsPushToTransferService;
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

    @Autowired
    XieChengSmsPushToTransferService xieChengSmsPushToTransferService;

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
        consumerService.consumerRun(channel, message, pushDataService::pushDassData, o, "");
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
     *
     * @param channel
     * @param message
     */
    @RabbitListener(queues = MQConstants.MARKETING_PUSH_TWOSEVEN_FILETRANSFER, containerFactory = "containerFactory")
    public void consumerPushTwoSeven(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushDataService::pushSevenTransferData, o, "");
    }

//    /**
//     * 消费海尔消转化数据
//     *
//     * @param channel 通道
//     * @param message 消息
//     */
//    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_QUEUE_PUSH_TRANSFER_HAIER, durable = "true")
//            , exchange = @Exchange(value = MQConstants.MARKETINGEXCHANGER_NAME, type = "topic", durable = "true")
//            , key = MQConstants.ROUTING_KEY_MARKETING_QUEUE_PUSH_TRANSFER_HAIER)}, containerFactory = "containerFactory")
//    public void consumerPushTransferHaier(Channel channel, Message message) {
//        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
//        }.getType());
//        consumerService.consumerRun(channel, message, pushDataService::pushHaierTransferData, o, "");
//    }。

    /**
     * 消费sftpToDb数据
     *
     * @param channel
     * @param message
     */
    @RabbitListener(queues = MQConstants.MARKETING_UNIVERSAL_SFTPTODB_RECEIVE, containerFactory = "containerFactory")
    public void consumerPushYiQianBao(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushDataService::pushSftpToDbData, o, "");
    }

    /**
     * 消费 携程消费
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE, durable = "true")
            , exchange = @Exchange(value = MQConstants.MARKETINGEXCHANGER_NAME, type = "topic", durable = "true")
            , key = MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE)}, containerFactory = "concurrentContainerFactory")
    public void xieChengToDb(Channel channel, Message message) {
        String mes  = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, pushDataService::pushXieChengToDbData, mes, null);
    }

    ///**
    // * 消费 携程短信撞库消费
    // *
    // * @param channel 通道
    // * @param message 消息体
    // */
    //@RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_UNIVERSAL_SFTPTODB_XIECHENGSMSCOLLIDINGRECEIVE, durable = "true")
    //        , exchange = @Exchange(value = MQConstants.MARKETINGEXCHANGER_NAME, type = "topic", durable = "true")
    //        , key = MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGSMSCOLLIDINGRECEIVE)}, containerFactory = "containerFactory")
    //public void xieChengSmsCollidingToDb(Channel channel, Message message) {
    //    String mes  = new String(message.getBody(), StandardCharsets.UTF_8);
    //    /*消费逻辑*/
    //    consumerService.consumerRun(channel, message, pushDataService::pushXieChengSmsCollidingToDbData, mes, null);
    //}
    /**
     * 推送dass转化
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_PUSH_DASS_TRANSFER, durable = "true")
            , exchange = @Exchange(value = MQConstants.MARKETINGEXCHANGER_NAME, type = "topic", durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_TRANSFER)}, containerFactory = "containerFactory")
    public void consumerPushDassTransfer(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushDataService::pushDassTransferData, o, "");
    }
    /**
     * 推送dassIBU
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_PUSH_DASS_IBU, durable = "true")
            , exchange = @Exchange(value = MQConstants.MARKETINGEXCHANGER_NAME, type = "topic", durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_IBU)}, containerFactory = "containerFactory")
    public void consumerPushDassIbu(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushDataService::pushDassTransferIbu, o, "");
    }

    /**
     * 消费 携程短信撞库数据推送客服接口导入异步处理
     * 携程新场景短信撞库result=false的sha256Code手机号
     *
     * @param channel 通道
     * @param message 消息体
     */

    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_XIECHENG_SMSCOLLIDINGVT_CUSTOMER, durable = "true")
            , exchange = @Exchange(value = MQConstants.MARKETINGEXCHANGER_NAME, type = "topic", durable = "true")
            , key = MQConstants.ROUTING_KEY_XIECHENG_SMSCOLLIDINGVT_CUSTOMER)}, containerFactory = "containerFactory")
    public void consumerXiechengSmsCollidingVtUser(Channel channel, Message message) {
        log.warn("Marketing_XieChengSmsCollidingVt_Customer：获取消息成功");
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        consumerService.consumerRun(channel, message, xieChengSmsPushToTransferService::consumerXiechengSmsCollidingVtUser, o, null);
    }
}
