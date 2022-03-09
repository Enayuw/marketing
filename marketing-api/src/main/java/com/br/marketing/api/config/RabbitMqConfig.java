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
     * marketing 营销平台接受预处理人员队列
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_PRE_USER_RECEIVE)
    public Queue preUserQueue() {
        return new Queue(MQConstants.MARKETING_PRE_USER_RECEIVE, true);
    }

    /**
     * 绑定——营销平台接受预处理人员队列
     *
     * @return
     */
    @Bean
    public Binding preUserBinding() {
        return BindingBuilder.bind(preUserQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_PRE_USER_RECEIVE);
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
     * marketing 转化数据队列
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_TRANSFER_RECEIVE)
    public Queue transferQueue() {
        return new Queue(MQConstants.MARKETING_TRANSFER_RECEIVE, true);
    }

    /**
     * 绑定——转化数据队列
     *
     * @return
     */
    @Bean
    public Binding transferBinding() {
        return BindingBuilder.bind(transferQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_RECEIVE);
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
