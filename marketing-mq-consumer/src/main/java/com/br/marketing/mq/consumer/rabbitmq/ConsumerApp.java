package com.br.marketing.mq.consumer.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.zhongyou.ZhongYouDataService;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.xiecheng.XieChengActivateDTO;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.service.Impl.wuba.WuBaCollidingDataQueryResultService;
import com.br.marketing.service.Impl.wuba.WuBaOldCollidingDataQueryResultService;
import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.VariableDicService;
import com.br.marketing.service.XieChengSmsPushToTransferService;
import com.br.marketing.service.clean.common.DataCleanService;
import com.br.marketing.service.clean.guomei.GuoMeiDataCleanService;
import com.br.marketing.service.clean.hengchang.HengChangDataCleanService;
import com.br.marketing.service.clean.weiju.WeiJuDataCleanService;
import com.br.marketing.service.mark.PpRonShuMarkService;
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
    PushDataService pushDataService;

    @Resource
    private VariableDicService variableDicService;

    @Resource
    private ZhongYouDataService zhongYouDataService;

    @Resource
    private XieChengRobDataCollidingService robDataCollidingService;

    @Resource
    private WeiJuDataCleanService weiJuDataCleanService;
    @Resource
    private GuoMeiDataCleanService guoMeiDataCleanService;
    @Resource
    private HengChangDataCleanService hengChangDataCleanService;
    @Autowired
    XieChengSmsPushToTransferService xieChengSmsPushToTransferService;
    @Resource
    private WuBaCollidingDataQueryResultService wuBaCollidingDataQueryResultService;
    @Resource
    private PpRonShuMarkService ppRonShuMarkService;
    @Resource
    private WuBaOldCollidingDataQueryResultService wuBaOldCollidingDataQueryResultService;

    @Resource
    private DataCleanService dataCleanService;


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
     * 消费 携程促活数据接入消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_XIECHENG_COLLIDING_ACTIVATE_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_ACTIVATE)}, containerFactory = "concurrentContainerFactory")
    public void consumerXieChengActivate(Channel channel, Message message) {
        XieChengActivateDTO xieChengActivateDTO = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8),
                new TypeReference<XieChengActivateDTO>() {
                }.getType());
        consumerService.consumerRun(channel, message, robDataCollidingService::activateDataHandle, xieChengActivateDTO, null);
    }


    /**
     * 消费 微聚数据清洗消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_WEIJU_DATA_CLEAN_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_WEIJU_DATA_CLEAN)}, containerFactory = "concurrentContainerFactory")
    public void consumerWeiJuDataClean(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, weiJuDataCleanService::cleanData, new String(message.getBody(), StandardCharsets.UTF_8), null);
    }


    /**
     * 消费 国美数据清洗消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_GUOMEI_DATA_CLEAN_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_GUOMEI_DATA_CLEAN)}, containerFactory = "concurrentContainerFactory")
    public void consumerGuoMeiDataClean(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, guoMeiDataCleanService::cleanData, new String(message.getBody(), StandardCharsets.UTF_8), null);
    }


    /**
     * 消费 国美数据清洗消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_GUOMEI_BLACK_DATA_CLEAN_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_GUOMEI_BLACK_DATA_CLEAN)}, containerFactory = "concurrentContainerFactory")
    public void consumerGuoMeiBlackDataClean(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, guoMeiDataCleanService::cleanBlackData, new String(message.getBody(), StandardCharsets.UTF_8), null);
    }

    /**
     * 消费 恒昌数据清洗消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_HENGCHANG_DATA_CLEAN_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_HENGCHANG_DATA_CLEAN)}, containerFactory = "concurrentContainerFactory")
    public void consumerHengChangDataClean(Channel channel, Message message) {
        consumerService.consumerRun(channel, message, hengChangDataCleanService::cleanData, new String(message.getBody(), StandardCharsets.UTF_8), null);
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
            , key = MQConstants.ROUTING_KEY_XIECHENG_SMSCOLLIDINGVT_CUSTOMER)}, containerFactory = "xieChengSmsMqContainerFactory")
    public void consumerXiechengSmsCollidingVtUser(Channel channel, Message message) {
        String o = new String(message.getBody(), StandardCharsets.UTF_8);
        consumerService.consumerRun(channel, message, xieChengSmsPushToTransferService::consumerXiechengSmsCollidingVtUser, o, null);
    }

    /**
     * 消费 58撞库status=-1数据消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_WUBA_COLLIDING_ELIMINATE_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_WUBA_COLLIDING_ELIMINATE)}, containerFactory = "concurrentContainerFactory")
    public void consumerWuBaCollidingEliminate(Channel channel, Message message) {
        String batchIdStr = new String(message.getBody(), StandardCharsets.UTF_8);
        consumerService.consumerRun(channel, message, wuBaCollidingDataQueryResultService::buildEliminateAndPushToRobot, batchIdStr, null);
    }

    /**
     * 消费 pp榕树打标生成清洗任务消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_PP_RONGSHU_MARK_CREATE_CLEAN_TASK_QUEUE, durable = "true")
            , exchange = @Exchange(value = MQConstants.MARKETINGEXCHANGER_NAME, type = "topic", durable = "true")
            , key = MQConstants.ROUTING_KEY_PP_RONGSHU_MARK_CREATE_CLEAN_TASK)}, containerFactory = "concurrentContainerFactory")
    public void consumerpPRongShuMark(Channel channel, Message message) {
        String localIdStr = new String(message.getBody(), StandardCharsets.UTF_8);
        Long localId = Long.valueOf(localIdStr);
        /*消费逻辑*/
        consumerService.consumerRun(channel, message, ppRonShuMarkService::createCleanTask, localId, null);
    }

    /**
     * 消费 58撞库status=-1数据消费端
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_WUBA_OLD_COLLIDING_ELIMINATE_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_WUBA_OLD_COLLIDING_ELIMINATE)}, containerFactory = "concurrentContainerFactory")
    public void consumerWuBaOldCollidingEliminate(Channel channel, Message message) {
        String batchIdStr = new String(message.getBody(), StandardCharsets.UTF_8);
        consumerService.consumerRun(channel, message, wuBaOldCollidingDataQueryResultService::buildEliminateAndPushToRobot, batchIdStr, null);
    }

    /**
     * 消费 客户原始数据json解析
     *
     * @param channel 通道
     * @param message 消息体
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_CUSTOMER_DATA_JSON_PARSE_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_CUSTOMER_DATA_JSON_PARSE)}, containerFactory = "fiveDataContainerFactory")
    public void consumerCustomerDataJsonParse(Channel channel, Message message) {

        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        consumerService.consumerRun(channel, message, dataCleanService::customerDataJsonParse, msg, null);
    }

    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MQConstants.MARKETING_COMMON_DATA_JSON_PARSE_QUEUE, durable = "true")
            , exchange = @Exchange(type = "topic", value = MQConstants.MARKETINGEXCHANGER_NAME, durable = "true")
            , key = MQConstants.ROUTING_KEY_MARKETING_COMMON_DATA_JSON_PARSE)}, containerFactory = "fiveDataContainerFactory")
    public void consumerCommonDataJsonParse(Channel channel, Message message) {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        consumerService.consumerRun(channel, message, dataCleanService::commonDataJsonParse, msg, null);
    }


}
