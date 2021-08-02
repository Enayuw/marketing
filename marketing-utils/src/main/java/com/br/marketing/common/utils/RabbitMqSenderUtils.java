package com.br.marketing.common.utils;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitMqSenderUtils {

    public static void convertAndSendPriority(RabbitTemplate rabbitTemplate
            , String topicExchange, String routingKey, String msg
    ) {
        MessagePostProcessor messagePostProcessor = (Message arg0) -> {
            arg0.getMessageProperties().setContentEncoding("utf-8");
            return arg0;
        };
        rabbitTemplate.convertAndSend(topicExchange, routingKey, msg, messagePostProcessor);
    }

}
