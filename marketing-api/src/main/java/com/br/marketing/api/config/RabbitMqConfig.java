package com.br.marketing.api.config;

import com.br.marketing.common.utils.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMqConfig {
    public static final int MQ_LISTENER = 2;

    @Bean(name = MQConstants.MarketingexchangerName)
    public TopicExchange warningExchange() {
        return new TopicExchange(MQConstants.MarketingexchangerName,true,false);
    }


    @Bean(name = MQConstants.MarketingexchangerDeadName)
    public TopicExchange deadLetterExchange(){
        return new TopicExchange(MQConstants.MarketingexchangerDeadName,true,false);
    }

    @Bean("delayQueueA")
    public Queue delayQueueA(){
//        Map<String, Object> args = new HashMap<>(2);
//        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
//        args.put("x-dead-letter-exchange", MQConstants.MarketingexchangerDeadName);
//        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
//        args.put("x-dead-letter-routing-key", MQConstants.RoutingKey_Marketing_Push_CustomerService_Search_Delay);
//        // x-message-ttl  声明队列的TTL
//        args.put("x-message-ttl", 6000);
//        return QueueBuilder.durable(DELAY_QUEUEA_NAME).withArguments(args).build();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean("containerFactory")
    public SimpleRabbitListenerContainerFactory containerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                         ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //设置线程数
        factory.setConcurrentConsumers(MQ_LISTENER);
        //最大线程数
        factory.setMaxConcurrentConsumers(MQ_LISTENER);
        factory.setPrefetchCount(0);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
