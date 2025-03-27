package com.br.marketing.push.consumer.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.push.service.impl.CheckFileServiceImpl;
import com.br.marketing.push.service.impl.MergeWithMessageServiceImpl;
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
    CheckFileServiceImpl checkFileService;

    @Autowired
    MergeWithMessageServiceImpl mergeWithMessageService;

    /**
     * 延迟消费 获取推送客服中心数据状态
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(queues = MQConstants.CHECK_QUEUE_NAME, containerFactory = "containerFactory")
    public void consumerPushDass(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, checkFileService::consumerFileCheck, o, "");
    }


    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_PUSHTASK_FILE_INITMERGE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_PUSHTASK_FILE_INITMERGE)}, containerFactory = "containerFactory")
    public void consumerInitFileMerge(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, mergeWithMessageService::consumerInitFileMsg, o, null);
    }


    /**
     * 延迟消费 获取推送客服中心数据状态
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_PUSHTASK_FILE_MERGE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_PUSHTASK_FILE_MERGE)
        ,@QueueBinding(value = @Queue(value = MQConstants.MARKETING_PUSHTASK_FILE_MERGE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_DEAD_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_PUSHTASK_FILE_MERGE)}, containerFactory = "containerFactory")
    public void consumerFileMerge(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, mergeWithMessageService::consumerFileMsg, o, MQConstants.ROUTING_KEY_PUSHTASK_FILE_MERGE_ERRORDELAY);
    }


    /**
     * 延迟消费 获取推送客服中心数据状态
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_OFFLINETASK_FILE_CALLBACK, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK)
            ,@QueueBinding(value = @Queue(value = MQConstants.MARKETING_OFFLINETASK_FILE_CALLBACK, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_DEAD_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK)}, containerFactory = "containerFactory")
    public void consumerOfflineCallBack(Channel channel, Message message) {
        Long o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), new TypeReference<Long>() {
        }.getType());
        consumerService.consumerRun(channel, message, mergeWithMessageService::consumerFileCallBack, o, MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK_ERRORDELAY);
    }
}
