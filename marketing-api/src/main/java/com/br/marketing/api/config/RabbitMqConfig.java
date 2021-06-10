package com.br.marketing.api.config;

import com.br.marketing.common.utils.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMqConfig {
    public static final int MQ_LISTENER = 2;

    /**
     * marketing 通用交换机
     * @return
     */
    @Bean(name = MQConstants.MarketingexchangerName)
    public TopicExchange gateExchange() {
        return new TopicExchange(MQConstants.MarketingexchangerName,true,false);
    }

    /**
     * marketing 通用死信交换机
     * @return
     */
    @Bean(name = MQConstants.MarketingexchangerDeadName)
    public TopicExchange deadGateExchange(){
        return new TopicExchange(MQConstants.MarketingexchangerDeadName,true,false);
    }

    /**
     * marketing 营销平台接受预处理人员队列
     * @return
     */
    @Bean(name = MQConstants.Marketing_PreUser_Receive)
    public Queue preUserQueue(){
        return new Queue(MQConstants.Marketing_PreUser_Receive, true);
    }

    /**
     * 绑定——营销平台接受预处理人员队列
     * @return
     */
    @Bean
    public Binding preUserBinding(){
        return BindingBuilder.bind(preUserQueue()).to(gateExchange()).with(MQConstants.RoutingKey_Marketing_PreUser_Receive);
    }

    /**
     * 延迟队列-查询推送智能客服状态
     * @return
     */
    @Bean(name = MQConstants.Marketing_Push_CustomerService_Search_Delay)
    public Queue customerSearchDelayQueue(){
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", MQConstants.MarketingexchangerDeadName);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", MQConstants.RoutingKey_Marketing_Push_CustomerService_Search_Delay);
        // x-message-ttl  声明队列的TTL
        args.put("x-message-ttl", 3000);
        return QueueBuilder.durable(MQConstants.Marketing_Push_CustomerService_Search_Delay).withArguments(args).build();
    }

    /**
     * 绑定-查询推送智能客服绑定
     * @return
     */
    @Bean
    public Binding customerSearchDelayBinding(){
        return BindingBuilder.bind(customerSearchDelayQueue()).to(gateExchange()).with(MQConstants.RoutingKey_Marketing_Push_CustomerService_Search_topic);
    }

    /**
     * 消费队列-查询推送智能客服状态
     * @return
     */
    @Bean(name = MQConstants.Marketing_Push_CustomerService_Search)
    public Queue customerSearchQueue(){
        return new Queue(MQConstants.Marketing_Push_CustomerService_Search, true);
    }

    /**
     * 绑定死信交换机- 消费队列-查询推送智能客服状态
     * @return
     */
    @Bean
    public Binding customerSearchBinding(){
        return BindingBuilder.bind(customerSearchQueue()).to(deadGateExchange()).with(MQConstants.RoutingKey_Marketing_Push_CustomerService_Search_Delay);
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
        factory.setPrefetchCount(2);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
