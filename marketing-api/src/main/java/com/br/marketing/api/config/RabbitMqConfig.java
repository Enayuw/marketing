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
     * marketing 原始上传数据大队列
     * @return Queue
     */
    @Bean(name = MQConstants.MARKETING_PRE_USER_RECEIVE)
    public Queue preUserQueue() {
        return new Queue(MQConstants.MARKETING_PRE_USER_RECEIVE, true);
    }

    /**
     * 绑定原始上传数据大队列
     * @return Binding
     */
    @Bean
    public Binding preUserBinding() {
        return BindingBuilder.bind(preUserQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_PRE_USER_RECEIVE);
    }

    /**
     * marketing 原始上传数据小队列
     * @return Queue
     */
    @Bean(name = MQConstants.MARKETING_PREUSER_RECEIVE_SMALL)
    public Queue preUserQueueSmall() {
        Map<String, Object> args = new HashMap<>();
        // 设置队列的最大优先级为10
        args.put("x-max-priority", 10);
        return new Queue(MQConstants.MARKETING_PREUSER_RECEIVE_SMALL, true, false, false, args);
    }

    /**
     * 绑定原始上传数据小队列
     * @return Binding
     */
    @Bean
    public Binding preUserSmallBinding() {
        return BindingBuilder.bind(preUserQueueSmall()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_PREUSER_RECEIVE_SMALL);
    }

    /**
     * marketing 原始上传数据应急队列
     * @return Queue
     */
    @Bean(name = MQConstants.MARKETING_PREUSER_RECEIVE_EMERGENCY)
    public Queue preUserQueueEmergency() {
        Map<String, Object> args = new HashMap<>();
        // 设置队列的最大优先级为10
        args.put("x-max-priority", 10);
        return new Queue(MQConstants.MARKETING_PREUSER_RECEIVE_EMERGENCY, true, false, false, args);
    }

    /**
     * 绑定原始上传数据应急队列
     * @return Binding
     */
    @Bean
    public Binding preUserEmergencyBinding() {
        return BindingBuilder.bind(preUserQueueEmergency()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_PREUSER_RECEIVE_EMERGENCY);
    }

    /**
     * marketing 跑批人员入队列
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_USER_RECEIVE)
    public Queue userQueue() {
        return new Queue(MQConstants.MARKETING_USER_RECEIVE, true);
    }

    /**
     * 绑定——跑批人员队列
     *
     * @return
     */
    @Bean
    public Binding userBinding() {
        return BindingBuilder.bind(userQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_USER_RECEIVE);
    }

    /**
     * marketing 原始转化数据大队列
     * @return Queue
     */
    @Bean(name = MQConstants.MARKETING_TRANSFER_RECEIVE)
    public Queue transferQueue() {
        return new Queue(MQConstants.MARKETING_TRANSFER_RECEIVE, true);
    }

    /**
     * 绑定原始转化数据大队列
     * @return Binding
     */
    @Bean
    public Binding transferBinding() {
        return BindingBuilder.bind(transferQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_RECEIVE);
    }

    /**
     * marketing 原始转化数据小队列
     * @return Queue
     */
    @Bean(name = MQConstants.MARKETING_TRANSFER_RECEIVE_SMALL)
    public Queue transferQueueSmall() {
        Map<String, Object> args = new HashMap<>();
        // 设置队列的最大优先级为10
        args.put("x-max-priority", 10);
        return new Queue(MQConstants.MARKETING_TRANSFER_RECEIVE_SMALL, true, false, false, args);
    }

    /**
     * 绑定原始转化数据小队列
     * @return Binding
     */
    @Bean
    public Binding transferSmallBinding() {
        return BindingBuilder.bind(transferQueueSmall()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_RECEIVE_SMALL);
    }

    /**
     * marketing 原始转化数据应急队列
     * @return Queue
     */
    @Bean(name = MQConstants.MARKETING_TRANSFER_RECEIVE_EMERGENCY)
    public Queue transferQueueEmergency() {
        Map<String, Object> args = new HashMap<>();
        // 设置队列的最大优先级为10
        args.put("x-max-priority", 10);
        return new Queue(MQConstants.MARKETING_TRANSFER_RECEIVE_EMERGENCY, true, false, false, args);
    }

    /**
     * 绑定原始转化数据应急队列
     * @return Binding
     */
    @Bean
    public Binding transferEmergencyBinding() {
        return BindingBuilder.bind(transferQueueEmergency()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_RECEIVE_EMERGENCY);
    }

    /**
     * marketing 转化数据推送客服队列
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_TRANSFER_PUSH_CUSTOMER)
    public Queue transferPushCustomerQueue() {
        return new Queue(MQConstants.MARKETING_TRANSFER_PUSH_CUSTOMER, true);
    }

    /**
     * 绑定——转化数据推送客服队列
     *
     * @return
     */
    @Bean
    public Binding transferPushCustomerBinding() {
        return BindingBuilder.bind(transferPushCustomerQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER);
    }

    /**
     * 上传接口接收场景字典收集队列
     *
     * @return Queue 持久化队列
     */
    @Bean(name = MQConstants.MARKETING_UPLOAD_API_USERTYPE_COLLECTION)
    public Queue uploadUsertypeCollectionQueue() {
        return QueueBuilder.durable(MQConstants.MARKETING_UPLOAD_API_USERTYPE_COLLECTION).build();
    }

    /**
     * 上传接口接收场景字典收集队列绑定普通交换机
     *
     * @param delayQueue   场景字典收集队列
     * @param gateExchange 主题交换机
     * @return Queue 持久化队列
     */
    @Bean
    public Binding uploadUsertypeCollectionQueueBindingGateExchange(
            @Qualifier(MQConstants.MARKETING_UPLOAD_API_USERTYPE_COLLECTION) Queue delayQueue
            , @Qualifier(MQConstants.MARKETINGEXCHANGER_NAME) TopicExchange gateExchange) {
        return BindingBuilder.bind(delayQueue).to(gateExchange).with(
                MQConstants.BINDING_KEY_MARKETING_UPLOAD_API_COLLECTION_FRAGMENTS);
    }

    /**
     * 转化接口接收场景字典收集队列
     *
     * @return Queue 持久化队列
     */
    @Bean(name = MQConstants.MARKETING_TRANSFER_API_USERTYPE_COLLECTION)
    public Queue transferUsertypeCollectionQueue() {
        return QueueBuilder.durable(MQConstants.MARKETING_TRANSFER_API_USERTYPE_COLLECTION).build();
    }

    /**
     * 转化接口接收场景字典收集队列绑定普通交换机
     *
     * @param delayQueue   场景字典收集队列
     * @param gateExchange 主题交换机
     * @return Queue 持久化队列
     */
    @Bean
    public Binding transferUsertypeCollectionQueueBindingGateExchange(
            @Qualifier(MQConstants.MARKETING_TRANSFER_API_USERTYPE_COLLECTION) Queue delayQueue
            , @Qualifier(MQConstants.MARKETINGEXCHANGER_NAME) TopicExchange gateExchange) {
        return BindingBuilder.bind(delayQueue).to(gateExchange).with(
                MQConstants.BINDING_KEY_MARKETING_TRANSFER_API_COLLECTION_FRAGMENTS);
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
        return containerFactory(configurer, connectionFactory,null);
    }

    @Bean(name = "fiveDataContainerFactory")
    public SimpleRabbitListenerContainerFactory fiveDataContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("primaryConnectionFactory") ConnectionFactory connectionFactory) {
        return containerFactory(configurer, connectionFactory,5);
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
            ConnectionFactory connectionFactory,Integer prefetchCount) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        if(prefetchCount!=null&&prefetchCount>0){
            factory.setPrefetchCount(prefetchCount);
        }
        configurer.configure(factory, connectionFactory);
        return factory;
    }
}
