package com.br.marketing.mq.consumer.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.api.customer.service.CustomerTransferDataService;
import com.br.marketing.common.constants.PulsarSubscription;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.MarketingSyncUser;
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

import javax.annotation.PostConstruct;
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
    @Autowired
    IPushShuheDataService pushShuheDataService;

    @Resource
    private CustomerTransferDataService customerTransferDataService;

    @Autowired
    PushDataService pushDataService;

    @Resource
    private VariableDicService variableDicService;


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
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_CONFIG_DEFAULT_VALID_DATE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_CONFIG_DEFAULT_VALID_DATE)}, containerFactory = "fiveDataContainerFactory")
    public void consumerConfigDefaultValidDate(Channel channel, Message message) {
        MarketingSyncUser o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8)
                , new TypeReference<MarketingSyncUser>() {
                }.getType());
        consumerService.consumerRun(channel, message, periodOfValidityService::configValidDateDefault, o, null);
    }
    /**
     * 设置定制化默认有效期范围消费者(360)
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_CUSTOMIZE_CONFIG_DEFAULT_VALID_DATE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_CUSTOMIZE_CONFIG_DEFAULT_VALID_DATE)}, containerFactory = "fiveDataContainerFactory")
    public void consumersCustomizeConfigDefaultValidDate(Channel channel, Message message) {
        MarketingSyncUser o = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8)
                , new TypeReference<MarketingSyncUser>() {
                }.getType());
        consumerService.consumerRun(channel, message, periodOfValidityService::customizeConfigValidDateDefault, o, null);
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
        consumerService.consumerRun(channel, message, pushRuleService::HandleZhongYouData, o, null);
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
     * 场景收集队列消费端
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_STANDARD_API_USERTYPE_COLLECTION
            , durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_STANDARD_API_USERTYPE_COLLECTION)}
            , containerFactory = "concurrentContainerFactory")
    public void standardApiUsertypeCollection(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, variableDicService::batchAddUserTypeVariableDicTry
                , new String(message.getBody(), StandardCharsets.UTF_8), null);
    }

    /**
     * 延迟发送场景消息队列
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE
            , durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_DEAD_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE)}
            , containerFactory = "primaryContainerFactory")
    public void delaySendUserTypeMessage(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, variableDicService::delaySendUserTypeMessage
                , new String(message.getBody(), StandardCharsets.UTF_8), null);
    }


    @PostConstruct
    void init() {
        // 标准上传数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.upLoadSubscription, pushRuleService::consumerSyncInfo, 2, PulsarTopic.upLoadTopic);

        // 数禾上传数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.upLoadShSubscription, pushShuheDataService::consumerShUpload, 2, PulsarTopic.upLoadShTopic);

        //标准转化数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.transferSubscription, pushRuleService::consumerTransferInfo, 2, PulsarTopic.transferTopic);

        //数禾转化数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.transferShSubscription, pushShuheDataService::consumerShTransfer, 2, PulsarTopic.transferShTopic);

        // 定制客户转化数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.transferCustomSubscription
                , customerTransferDataService::consumerTransferPayData, 2, PulsarTopic.transferCustomTopic);


    }
}
