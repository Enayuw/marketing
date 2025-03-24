package com.br.marketing.innerapi.config;

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
    @Bean(name = MQConstants.MARKETINGEXCHANGER_NAME)
    public TopicExchange gateExchange() {
        return new TopicExchange(MQConstants.MARKETINGEXCHANGER_NAME, true, false);
    }

    /**
     * marketing 通用死信交换机
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETINGEXCHANGER_DEAD_NAME)
    public TopicExchange deadGateExchange() {
        return new TopicExchange(MQConstants.MARKETINGEXCHANGER_DEAD_NAME, true, false);
    }


    /**
     * 消费队列-通用转化队列
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_UNIVERSAL_TRANSFER_RECEIVE)
    public Queue universalTransferQueue() {
        return new Queue(MQConstants.MARKETING_UNIVERSAL_TRANSFER_RECEIVE, true);
    }

    /**
     * 延迟队列-通用转化队列延迟队列
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY)
    public Queue universalTransferDelayQueue() {
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", MQConstants.MARKETINGEXCHANGER_DEAD_NAME);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE);
        // x-message-ttl  声明队列的TTL
        //args.put("x-message-ttl", 3000);
        return QueueBuilder.durable(MQConstants.MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY).withArguments(args).build();
    }

    /**
     * 延迟队列-通用转化队列延迟队列
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALF_HOUR)
    public Queue universalTransferDelayHalfHourQueue() {
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", MQConstants.MARKETINGEXCHANGER_DEAD_NAME);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE);
        // x-message-ttl  声明队列的TTL
        args.put("x-message-ttl", 1800000);
        return QueueBuilder.durable(MQConstants.MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALF_HOUR).withArguments(args).build();
    }

    /**
     * 延迟队列-通用转化错误重试延迟队列
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_UNIVERSAL_TRANSFER_ERROR_DELAY)
    public Queue universalTransferErrorDelayQueue() {
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", MQConstants.MARKETINGEXCHANGER_DEAD_NAME);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE);
        // x-message-ttl  声明队列的TTL 5分钟静置时间
        args.put("x-message-ttl", 300000);
        return QueueBuilder.durable(MQConstants.MARKETING_UNIVERSAL_TRANSFER_ERROR_DELAY).withArguments(args).build();
    }

    /**
     * 绑定死信交换机- 延迟队列通过死信交换机推送到延迟队列
     *
     * @return
     */
    @Bean
    public Binding universalTransferQueueBinding() {
        return BindingBuilder.bind(universalTransferQueue())
                .to(deadGateExchange())
                .with(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE);
    }

    /**
     * 绑定交换机- 发送消息到延迟队列
     *
     * @return
     */
    @Bean
    public Binding universalTransferQueueDelayBinding() {
        return BindingBuilder.bind(universalTransferDelayQueue())
                .to(gateExchange())
                .with(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY);
    }

    /**
     * 绑定交换机- 发送消息到延迟队列
     *
     * @return
     */
    @Bean
    public Binding universalTransferQueueErrorDelayBinding() {
        return BindingBuilder.bind(universalTransferErrorDelayQueue())
                .to(gateExchange())
                .with(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_ERROR_DELAY);
    }

    /**
     * 绑定交换机- 发送消息到延迟队列
     *
     * @return
     */
    @Bean
    public Binding universalTransferQueueDelayHalfHourBinding() {
        return BindingBuilder.bind(universalTransferDelayHalfHourQueue())
                .to(gateExchange())
                .with(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALF_HOUR);
    }

    /**
     * 消费队列-推送智能客服
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_PUSH_CUSTOMER_SERVICE)
    public Queue pushCustomerSearchQueue() {
        return new Queue(MQConstants.MARKETING_PUSH_CUSTOMER_SERVICE, true);
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
                .with(MQConstants.ROUTING_KEY_MARKETING_PUSH_CUSTOMER_SERVICE);
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

    @Bean(name = "secondaryContainerFactory")
    public SimpleRabbitListenerContainerFactory secondaryContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("secondaryConnectionFactory") ConnectionFactory connectionFactory) {
        return containerFactory(configurer, connectionFactory, null);
    }

    @Bean(name = "secondaryConnectionFactory")
    public ConnectionFactory secondaryConnectionFactory(
            @Value("${spring.rabbitmq.secondary.addresses}") String addresses,
            @Value("${spring.rabbitmq.secondary.username}") String username,
            @Value("${spring.rabbitmq.secondary.password}") String password,
            @Value("${spring.rabbitmq.secondary.virtual-host}") String virtualHost
    ) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(addresses);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
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
        return containerFactory(configurer, connectionFactory, null);
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
    @Bean(name = "fiveDataContainerFactory")
    public SimpleRabbitListenerContainerFactory fiveDataContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("primaryConnectionFactory") ConnectionFactory connectionFactory) {
        return containerFactory(configurer, connectionFactory, 5);
    }

    @Bean(name = "thirtyDataContainerFactory")
    public SimpleRabbitListenerContainerFactory thirtyDataContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("primaryConnectionFactory") ConnectionFactory connectionFactory) {
        return containerFactory(configurer, connectionFactory, 30);
    }

    /**
     * 2023-01-07 14:49
     * 通用队列监听工厂 alpha
     */
    @Bean(name = "universalDataContainerFactory")
    public SimpleRabbitListenerContainerFactory universalDataContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("primaryConnectionFactory") ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 预取
        factory.setPrefetchCount(30);
        //设置线程数
        factory.setConcurrentConsumers(2);
        //最大线程数
        factory.setMaxConcurrentConsumers(5);
        configurer.configure(factory, connectionFactory);
        return factory;
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
            ConnectionFactory connectionFactory, Integer prefetchCount) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        if (prefetchCount != null && prefetchCount > 0) {
            factory.setPrefetchCount(prefetchCount);
        }
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
