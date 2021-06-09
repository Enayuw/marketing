package com.br.marketing.api.config;

import com.br.marketing.common.utils.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {
    public static final int MQ_LISTENER = 2;

    @Bean(name = "gate")
    public TopicExchange warningExchange() {
        return new TopicExchange(MQConstants.exchangerName,true,false);
    }

    @Bean(name = "TaskQueue")
    public Queue taskQueue() {
        return new Queue(MQConstants.taskQueueName, true, false, false);
    }
    @Bean(name = "bindingTaskQueue")
    public Binding bindingTaskQueue() {
        return BindingBuilder.bind(taskQueue()).to(warningExchange()).with(MQConstants.taskRoutingKey);
    }

    @Bean(name = "pushQueue")
    public Queue pushQueue() {
        return new Queue(MQConstants.pushQueueName, true, false, false);
    }
    @Bean(name = "bindingPushQueue")
    public Binding bindingPushQueue() {
        return BindingBuilder.bind(pushQueue()).to(warningExchange()).with(MQConstants.pushRoutingKey);
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
