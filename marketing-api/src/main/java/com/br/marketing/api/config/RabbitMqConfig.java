package com.br.marketing.api.config;

import com.br.marketing.common.utils.MQConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RabbitMqConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqConfig.class);
    public static final int MQ_LISTENER = 2;

    /**
     * marketing 通用交换机
     *
     * @return
     */
    @Bean(name = MQConstants.MarketingexchangerName)
    public TopicExchange gateExchange() {
        return new TopicExchange(MQConstants.MarketingexchangerName, true, false);
    }

    /**
     * marketing 通用死信交换机
     *
     * @return
     */
    @Bean(name = MQConstants.MarketingexchangerDeadName)
    public TopicExchange deadGateExchange() {
        return new TopicExchange(MQConstants.MarketingexchangerDeadName, true, false);
    }

    /**
     * marketing 营销平台接受预处理人员队列
     *
     * @return
     */
    @Bean(name = MQConstants.Marketing_PreUser_Receive)
    public Queue preUserQueue() {
        return new Queue(MQConstants.Marketing_PreUser_Receive, true);
    }

    /**
     * 绑定——营销平台接受预处理人员队列
     *
     * @return
     */
    @Bean
    public Binding preUserBinding() {
        return BindingBuilder.bind(preUserQueue()).to(gateExchange()).with(MQConstants.RoutingKey_Marketing_PreUser_Receive);
    }

    /**
     * marketing 跑批人员入队列
     *
     * @return
     */
    @Bean(name = MQConstants.Marketing_User_Receive)
    public Queue UserQueue() {
        return new Queue(MQConstants.Marketing_User_Receive, true);
    }

    /**
     * 绑定——跑批人员队列
     *
     * @return
     */
    @Bean
    public Binding UserBinding() {
        return BindingBuilder.bind(UserQueue()).to(gateExchange()).with(MQConstants.RoutingKey_Marketing_User_Receive);
    }

    /**
     * 延迟队列-查询推送智能客服状态
     *
     * @return
     */
    @Bean(name = MQConstants.Marketing_Push_CustomerService_Search_Delay)
    public Queue customerSearchDelayQueue() {
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
     *
     * @return
     */
    @Bean
    public Binding customerSearchDelayBinding() {
        return BindingBuilder.bind(customerSearchDelayQueue())
                .to(gateExchange())
                .with(MQConstants.RoutingKey_Marketing_Push_CustomerService_Search_Delay);
    }

    /**
     * 消费队列-查询推送智能客服状态
     *
     * @return
     */
    @Bean(name = MQConstants.Marketing_Push_CustomerService_Search)
    public Queue customerSearchQueue() {
        return new Queue(MQConstants.Marketing_Push_CustomerService_Search, true);
    }

    /**
     * 绑定死信交换机- 消费队列-查询推送智能客服状态
     *
     * @return
     */
    @Bean
    public Binding customerSearchBinding() {
        return BindingBuilder.bind(customerSearchQueue())
                .to(deadGateExchange())
                .with(MQConstants.RoutingKey_Marketing_Push_CustomerService_Search_Delay);
    }

    /**
     * 消费队列-推送智能客服
     *
     * @return
     */
    @Bean(name = MQConstants.Marketing_Push_CustomerService)
    public Queue pushCustomerSearchQueue() {
        return new Queue(MQConstants.Marketing_Push_CustomerService, true);
    }

    /**
     * 绑定交换机- 消费队列-推送智能客服
     *
     * @return
     */
    @Bean
    public Binding pushCustomerSearchBinding() {
        return BindingBuilder.bind(pushCustomerSearchQueue())
                .to(gateExchange())
                .with(MQConstants.RoutingKey_Marketing_Push_CustomerService);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }


    @Value("${cluster.flag}")
    private String clusterConfig;

    /**
     * 设置连接参数等信息
     *
     * @param zwAddresses
     * @param zwUsername
     * @param zwPassword
     * @param zwVirtualHost
     * @param yzAddresses
     * @param yzUsername
     * @param yzPassword
     * @param yzVirtualHost
     * @return
     */

    @Bean(name = "primaryConnectionFactory")
    @Primary
    public ConnectionFactory connectionFactory(
            @Value("${spring.rabbitmq.zw.addresses}") String zwAddresses,
            @Value("${spring.rabbitmq.zw.username}") String zwUsername,
            @Value("${spring.rabbitmq.zw.password}") String zwPassword,
            @Value("${spring.rabbitmq.zw.virtual-host}") String zwVirtualHost,
            @Value("${spring.rabbitmq.yz.addresses:11}") String yzAddresses,
            @Value("${spring.rabbitmq.yz.username:11}") String yzUsername,
            @Value("${spring.rabbitmq.yz.password:11}") String yzPassword,
            @Value("${spring.rabbitmq.yz.virtual-host:11}") String yzVirtualHost) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        String enumName = ClusterEnum.CLUSTER_PROD_C.getName();
        log.warn("clusterConfig:{},enumName:{}", clusterConfig, enumName);
        if (StringUtils.isNotBlank(clusterConfig) && enumName.equals(clusterConfig)) {
            connectionFactory.setAddresses(yzAddresses);
            connectionFactory.setUsername(yzUsername);
            connectionFactory.setPassword(yzPassword);
            connectionFactory.setVirtualHost(yzVirtualHost);
        } else {
            connectionFactory.setAddresses(zwAddresses);
            connectionFactory.setUsername(zwUsername);
            connectionFactory.setPassword(zwPassword);
            connectionFactory.setVirtualHost(zwVirtualHost);
        }
        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }


    @Bean
    @Primary
    public RabbitTemplate primaryRabbitTemplate(
            @Qualifier("primaryConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate primaryRabbitTemplate = new RabbitTemplate(connectionFactory);
        return primaryRabbitTemplate;
    }


    /**
     * factory：
     * 可设置的信息：
     * 1、消费线程数
     * 2、消费最大线程树
     * 3、.....
     * 等等rabbitMQ队列的配置信息
     *
     * @param configurer
     * @param connectionFactory
     * @return
     */
    @Bean(name = "primaryContainerFactory")
    public SimpleRabbitListenerContainerFactory primaryContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("primaryConnectionFactory") ConnectionFactory connectionFactory) {
        return containerFactory(configurer, connectionFactory);
    }

    /**
     * 配置
     *
     * @param configurer
     * @param connectionFactory
     * @return
     */
    private SimpleRabbitListenerContainerFactory containerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
