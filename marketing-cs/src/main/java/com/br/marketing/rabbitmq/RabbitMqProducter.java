package com.br.marketing.rabbitmq;


import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class RabbitMqProducter {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqProducter.class);

    private static final String exchange = "gate";

    @Autowired
    @Qualifier(value = "primaryRabbitTemplate")
    private RabbitTemplate rabbitTemplate;

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
}
