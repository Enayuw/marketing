package com.br.marketing.mq.consumer.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.zhongyou.ZhongYouDataService;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.*;
import com.br.marketing.service.Impl.ConsumerService;
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

    @Autowired
    IPushShuheDataService pushShuheDataService;

    @Autowired
    PushDataService pushDataService;

    @Resource
    private VariableDicService variableDicService;

    @Resource
    private MarketingSyncReportService marketingSyncReportService;

    @Resource
    private TransferSyncReportService transferSyncReportService;

    @Resource
    private ZhongYouDataService zhongYouDataService;

    /**
     * 消费 原始上传数据消费端（大队列）
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
     * 消费 原始上传数据消费端（小队列）
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = MQConstants.MARKETING_PREUSER_RECEIVE_SMALL, containerFactory = "fiveDataContainerFactory")
    public void consumerPreUserSmall(Channel channel, Message message) {
        log.warn("MARKETING_PREUSER_RECEIVE_SMALL：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::insertMarketingPreUserSync, o, null);
    }

    /**
     * 消费 原始上传数据消费端（应急队列）
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = MQConstants.MARKETING_PREUSER_RECEIVE_EMERGENCY, containerFactory = "fiveDataContainerFactory")
    public void consumerPreUserEmergency(Channel channel, Message message) {
        log.warn("MARKETING_PREUSER_RECEIVE_EMERGENCY：获取消息成功");
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
     * 消费 原始转化数据消费端（大队列）
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
     * 消费 原始转化数据消费端（小队列）
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = MQConstants.MARKETING_TRANSFER_RECEIVE_SMALL, containerFactory = "fiveDataContainerFactory")
    public void consumerTransferUserSmall(Channel channel, Message message) {
        log.warn("MARKETING_TRANSFER_RECEIVE_SMALL：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::consumerTransferData, o, null);
    }

    /**
     * 消费 原始转化数据消费端（应急队列）
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = MQConstants.MARKETING_TRANSFER_RECEIVE_EMERGENCY, containerFactory = "fiveDataContainerFactory")
    public void consumerTransferUserEmergency(Channel channel, Message message) {
        log.warn("MARKETING_TRANSFER_RECEIVE_EMERGENCY：获取消息成功");
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, pushRuleService::consumerTransferData, o, null);
    }

    /**
     * 消费 中邮清洗数据
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_ZHONGYOU_DATA_CLEAN, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_ZHONGYOU_DATA_CLEAN)}, containerFactory = "fiveDataContainerFactory")
    public void consumerZhongYouData(Channel channel, Message message) {

        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, zhongYouDataService::HandleZhongYouData, o, null);
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
        String mes = new String(message.getBody(), StandardCharsets.UTF_8);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, pushDataService::pushXieChengToDbData, mes, null);
    }

    /**
     * 上传场景收集队列消费端
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_UPLOAD_API_USERTYPE_COLLECTION
            , durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.BINDING_KEY_MARKETING_UPLOAD_API_COLLECTION_FRAGMENTS)}
            , containerFactory = "consumerTenPrefetchTwoFactory")
    public void uploadApiUsertypeCollection(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, variableDicService::batchAddUserTypeVariableDicTry
                , new String(message.getBody(), StandardCharsets.UTF_8), null);
    }

    /**
     * 转化场景收集队列消费端
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_TRANSFER_API_USERTYPE_COLLECTION
            , durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.BINDING_KEY_MARKETING_TRANSFER_API_COLLECTION_FRAGMENTS)}
            , containerFactory = "consumerTenPrefetchTwoFactory")
    public void transferApiUsertypeCollection(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, variableDicService::batchAddUserTypeVariableDicTry
                , new String(message.getBody(), StandardCharsets.UTF_8), null);
    }

    /**
     * 发送场景消息死信队列
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_SEND_USERTYPE_MESSAGE_DEAD_QUEUE
            , durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_DEAD_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_SEND_USERTYPE_MESSAGE_DEAD_QUEUE)}
            , containerFactory = "primaryContainerFactory")
    public void delaySendUserTypeMessage(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, variableDicService::delaySendUserTypeMessage
                , new String(message.getBody(), StandardCharsets.UTF_8), null);
    }


    /**
     * 上传接口接收数据量级碎片队列消费端
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_UPLOAD_API_DATA_COUNT_FRAGMENTS
            , durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.BINDING_KEY_MARKETING_UPLOAD_API_COLLECTION_FRAGMENTS)}
            , containerFactory = "consumerTenPrefetchTwoFactory")
    public void uploadDataCountFragments(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, marketingSyncReportService::nearRealtimeDataCountFragmentsStatis
                , new String(message.getBody(), StandardCharsets.UTF_8), null);
    }

    /**
     * 转化接口接收数据量级碎片队列消费端
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_TRANSFER_API_DATA_COUNT_FRAGMENTS
            , durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.BINDING_KEY_MARKETING_TRANSFER_API_COLLECTION_FRAGMENTS)}
            , containerFactory = "consumerTenPrefetchTwoFactory")
    public void transferDataCountFragments(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, transferSyncReportService::nearRealtimeDataCountFragmentsStatis
                , new String(message.getBody(), StandardCharsets.UTF_8), null);
    }

}
