package com.br.marketing.xc.config;

import com.br.marketing.common.enums.ClusterEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMqConfig {
    public static final int MQ_LISTENER = 1;

    @Value("${cluster.flag}")
    private String clusterConfig;

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

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
        String _yzProNm = ClusterEnum.CLUSTER_PROD_C.getName();
        String _yzSimNm = ClusterEnum.CLUSTER_PROD_D.getName();
        if (StringUtils.isNotBlank(clusterConfig) && (_yzProNm.equals(clusterConfig) || _yzSimNm.equals(clusterConfig))) {
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

    @Bean(name = "containerFactory")
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
