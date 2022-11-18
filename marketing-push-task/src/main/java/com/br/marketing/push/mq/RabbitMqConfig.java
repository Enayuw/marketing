package com.br.marketing.push.mq;


import com.br.marketing.common.utils.MQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    /**
     * marketing 通用交换机
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETINGEXCHANGER_NAME)
    public TopicExchange gateExchange() {
        return new TopicExchange(MQConstants.MARKETINGEXCHANGER_NAME, true, false);
    }


    @Bean(name = MQConstants.CHECK_QUEUE_NAME)
    public Queue checkQueue() {
        return new Queue(MQConstants.CHECK_QUEUE_NAME, true, false, false);
    }

    @Bean(name = "bindinCheckQueue")
    public Binding bindingCheckQueue() {
        return BindingBuilder.bind(checkQueue()).to(gateExchange()).with(MQConstants.CHECK_ROUTING_KEY);
    }

    /**
     * 重试队列-离线文件合并重试
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_PUSHTASK_FILE_MERGE_ERRORDELAY)
    public Queue customerFileMergeDelayQueue() {
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", MQConstants.MARKETINGEXCHANGER_DEAD_NAME);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", MQConstants.ROUTING_KEY_PUSHTASK_FILE_MERGE);
        // x-message-ttl  声明队列的TTL
        args.put("x-message-ttl", 30000);
        return QueueBuilder.durable(MQConstants.MARKETING_PUSHTASK_FILE_MERGE_ERRORDELAY).withArguments(args).build();
    }

    @Bean(name = "bindingFileMergeDelayQueue")
    public Binding bindingFileMergeDelayQueue() {
        return BindingBuilder.bind(customerFileMergeDelayQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_PUSHTASK_FILE_MERGE_ERRORDELAY);
    }


    /**
     * 重试队列-获取离线文件重试
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETING_OFFLINETASK_FILE_CALLBACK_ERRORDELAY)
    public Queue customerFileCallBackDelayQueue() {
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", MQConstants.MARKETINGEXCHANGER_DEAD_NAME);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK);
        // x-message-ttl  声明队列的TTL
        args.put("x-message-ttl", 30000);
        return QueueBuilder.durable(MQConstants.MARKETING_OFFLINETASK_FILE_CALLBACK_ERRORDELAY).withArguments(args).build();
    }

    @Bean(name = "bindingFileCallBackDelayQueue")
    public Binding bindingFileCallBackDelayQueue() {
        return BindingBuilder.bind(customerFileCallBackDelayQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK_ERRORDELAY);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
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
