package com.br.marketing.bridge.mq;


import com.br.marketing.common.enums.ClusterEnum;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2020/9/11 17:09
 * @Description:
 **/
@Configuration
@Slf4j
public class RabbitMqConfig {
    public static final int MQ_LISTENER = 1;
    public static final int MQ_CONCURRENT_LISTENER = 5;

    @Value("${cluster.flag}")
    private String clusterConfig;

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

    @Bean(name = MQConstants.MARKETING_PUSH_DASS_SCORE)
    public Queue pushDassQueue() {
        return new Queue(MQConstants.MARKETING_PUSH_DASS_SCORE, true, false, false);
    }

    @Bean(name = MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_SCORE)
    public Binding bindingPushDassQueue() {
        return BindingBuilder.bind(pushDassQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_SCORE);
    }

    @Bean(name = MQConstants.MARKETING_PUSH_OUTBOUND_SCORE)
    public Queue pushOutBoundQueue() {
        return new Queue(MQConstants.MARKETING_PUSH_OUTBOUND_SCORE, true, false, false);
    }

    @Bean(MQConstants.ROUTING_KEY_MARKETING_PUSH_DATA_SCORE)
    public Binding bindingPushZhongYuanDassQueue() {
        return BindingBuilder.bind(pushDassQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_PUSH_DATA_SCORE);
    }

    @Bean(name = MQConstants.MARKETING_PUSH_TWOSEVEN_FILETRANSFER)
    public Queue pushSevenQueue() {
        return new Queue(MQConstants.MARKETING_PUSH_TWOSEVEN_FILETRANSFER, true, false, false);
    }

    @Bean(name = MQConstants.ROUTING_KEY_MARKETING_PUSH_TWOSEVEN_FILETRANSFER)
    public Binding bindingSevenQueue() {
        return BindingBuilder.bind(pushSevenQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_PUSH_TWOSEVEN_FILETRANSFER);
    }


    @Bean(name = MQConstants.MARKETING_UNIVERSAL_SFTPTODB_RECEIVE)
    public Queue pushSftpToDb() {
        return new Queue(MQConstants.MARKETING_UNIVERSAL_SFTPTODB_RECEIVE, true, false, false);
    }
    @Bean(name = MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_RECEIVE)
    public Binding bindingYiQianBao() {
        return BindingBuilder.bind(pushSftpToDb()).to(gateExchange()).with(MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_RECEIVE);
    }
    @Bean(name = MQConstants.MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE)
    public Queue pushSftpToDbXieCheng() {
        return new Queue(MQConstants.MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE, true, false, false);
    }
    @Bean(name = MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE)
    public Binding bindingXieCheng() {
        return BindingBuilder.bind(pushSftpToDbXieCheng()).to(gateExchange()).with(MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE);
    }

    /**
     * 延迟队列-查询推送智能客服状态
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY)
    public Queue customerSearchDelayQueue() {
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", MQConstants.MARKETINGEXCHANGER_DEAD_NAME);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", MQConstants.ROUTING_KEY_MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY);
        // x-message-ttl  声明队列的TTL
        args.put("x-message-ttl", 3000);
        return QueueBuilder.durable(MQConstants.MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY).withArguments(args).build();
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
                .with(MQConstants.ROUTING_KEY_MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY);
    }

    /**
     * 消费队列-查询推送智能客服状态
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH)
    public Queue customerSearchQueue() {
        return new Queue(MQConstants.MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH, true);
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
                .with(MQConstants.ROUTING_KEY_MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY);
    }

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
        if (StringUtils.isNotBlank(clusterConfig) && (_yzProNm.equals(clusterConfig)||_yzSimNm.equals(clusterConfig))) {
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

    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Bean(name = "concurrentContainerFactory")
    public SimpleRabbitListenerContainerFactory concurrentContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                 ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //设置线程数
        factory.setConcurrentConsumers(marketingCommonConfig.getXiechengMqThread());
        //最大线程数
        factory.setMaxConcurrentConsumers(marketingCommonConfig.getXiechengMqThread());
        factory.setPrefetchCount(10);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean(name = "xieChengSmsMqContainerFactory")
    public SimpleRabbitListenerContainerFactory xieChengSmsMqContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                 ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //设置线程数
        factory.setConcurrentConsumers(2);
        //最大线程数
        factory.setMaxConcurrentConsumers(5);
        factory.setPrefetchCount(50);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        configurer.configure(factory, connectionFactory);
        return factory;
    }

}
