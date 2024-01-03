package com.br.marketing.mq.config;

import com.br.marketing.common.utils.MQConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.TopicExchange;
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


    @Bean(name = "concurrentContainerFactory")
    public SimpleRabbitListenerContainerFactory concurrentContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                           ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //设置线程数
        factory.setConcurrentConsumers(1);
        //最大线程数
        factory.setMaxConcurrentConsumers(2);
        factory.setPrefetchCount(10);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
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
