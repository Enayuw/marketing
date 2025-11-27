package com.br.marketing.rabbitmq;


import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.SwitchMessageQueueEnum;
import com.br.marketing.common.utils.AiMQConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.MrpMqFact;
import com.br.marketing.service.PushRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class RabbitMqProducter {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqProducter.class);

    private static final String exchange = "gate";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    PushRuleService pushRuleService;

    @Autowired
    @Qualifier("clusterEnvironment")
    private String clusterEnvironment;

    @PostConstruct
    void init(){
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData,b,c)->{
            String id = correlationData != null ? correlationData.getId() : "";
            if (b) {
                log.info("消息确认成功, id:{}", id);
            } else {
                CorrelationDataHasContent data = (CorrelationDataHasContent) correlationData;
                log.error("消息未成功投递, id:{},content:{},cause:{}", id, JSON.toJSONString(data.getMessage()), c);
            }
        });
        rabbitTemplate.setReturnCallback((message,int1,str1,str2,str3)->{
            log.error("消息未成功投递, message:{}", message);
        });
    }

    /**
     * 发送mq信息
     * @param routeKey
     * @param message
     */
    public void send(String routeKey,String message){
        CorrelationData correlationData = new CorrelationDataHasContent(UUID.randomUUID().toString(),message);
        rabbitTemplate.convertAndSend(exchange,routeKey,message,arg0 -> {
//            arg0.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
            arg0.getMessageProperties().setContentEncoding("UTF-8");
            return arg0;
        },correlationData);
    }

    /**
     * 发送mq信息(支持优先级)
     * @param routeKey
     * @param message
     * @param priority 消息优先级
     */
    public void send(String routeKey,String message, Integer priority){
        CorrelationData correlationData = new CorrelationDataHasContent(UUID.randomUUID().toString(),message);
        rabbitTemplate.convertAndSend(exchange,routeKey,message,(Message arg0) -> {
            arg0.getMessageProperties().setContentEncoding("UTF-8");
            arg0.getMessageProperties().setPriority(priority);
            return arg0;
        },correlationData);
    }

    /**
     * 发送mq信息
     * @param routeKey
     * @param message
     */
    public void sendByExpiration(String routeKey, String message, String expireTime) {
        CorrelationData correlationData = new CorrelationDataHasContent(UUID.randomUUID().toString(), message);
        rabbitTemplate.convertAndSend(exchange, routeKey, message, arg0 -> {
            arg0.getMessageProperties().setContentEncoding("UTF-8");
            arg0.getMessageProperties().setExpiration(expireTime);
            return arg0;
        }, correlationData);
    }

    /**
     * 发送延时且设定优先级的消息
     *
     * @param routeKey   路由key
     * @param message    消息
     * @param expireTime 延迟时间，毫秒
     * @param priority   优先级，具体需根据队列中设定最大优先级内设置
     */
    public void sendByExpiration(String routeKey, String message, String expireTime, int priority) {
        CorrelationData correlationData = new CorrelationDataHasContent(UUID.randomUUID().toString(), message);
        rabbitTemplate.convertAndSend(exchange, routeKey, message, (Message arg0) -> {
            arg0.getMessageProperties().setContentEncoding("UTF-8");
            arg0.getMessageProperties().setExpiration(expireTime);
            arg0.getMessageProperties().setPriority(priority);
            return arg0;
        }, correlationData);
    }

    /**
     * 发送mq信息
     *
     * @param mqFact
     */
    public void sendToUniversalTransferQueue(MqFact mqFact) {
        String message = JSON.toJSONString(mqFact);
        CorrelationData correlationData = new CorrelationDataHasContent(UUID.randomUUID().toString(), message);
        rabbitTemplate.convertAndSend(exchange, MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE, message, arg0 -> {
            arg0.getMessageProperties().setContentEncoding("UTF-8");
            return arg0;
        },correlationData);
    }

    public void sendToUniversalTransferQueue(MrpMqFact mrpMqFact) {
        String message = JSON.toJSONString(mrpMqFact);
        CorrelationData correlationData = new CorrelationDataHasContent(UUID.randomUUID().toString(), message);
        rabbitTemplate.convertAndSend(exchange, MQConstants.ROUTING_KEY_MRP_UNIVERSAL_TRANSFER, message, arg0 -> {
            arg0.getMessageProperties().setContentEncoding("UTF-8");
            return arg0;
        },correlationData);
    }

    /**
     * 发送携程拨打mq信息
     *
     * @param mqFact
     */
    public void sendToXieChengUniversalTransferQueue(MqFact mqFact) {
        String message = JSON.toJSONString(mqFact);
        CorrelationData correlationData = new CorrelationDataHasContent(UUID.randomUUID().toString(), message);
        rabbitTemplate.convertAndSend(exchange, MQConstants.ROUTING_KEY_XIECHENG_UNIVERSAL_TRANSFER_RECEIVE, message, arg0 -> {
            arg0.getMessageProperties().setContentEncoding("UTF-8");
            return arg0;
        },correlationData);
    }

    public void sendToAIUniversalQueue(MqFact mqFact) {
        String message = JSON.toJSONString(mqFact);
        CorrelationData correlationData = new CorrelationDataHasContent(UUID.randomUUID().toString(), message);

        String redisKey = RedisKeyConstant.SWITCH_MESSAGE_QUEUE + ":" + clusterEnvironment;
        String field = SwitchMessageQueueEnum.MARKETING_AI_UNIVERSAL_RECEIVE.name();
        String aiUniversalRoutingKey = SwitchMessageQueueEnum.MARKETING_AI_UNIVERSAL_RECEIVE.getDefault_route_key();
        String routingKeyFromRedis = pushRuleService.getRoutingKeyFromRedis(redisKey, field, aiUniversalRoutingKey);

        rabbitTemplate.convertAndSend(exchange, routingKeyFromRedis, message, arg0 -> {
            arg0.getMessageProperties().setContentEncoding("UTF-8");
            return arg0;
        }, correlationData);
    }
}
