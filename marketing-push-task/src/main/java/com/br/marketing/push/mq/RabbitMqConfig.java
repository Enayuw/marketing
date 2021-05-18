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
    public static final int MQ_LISTENER = 2;
    @Bean(name = "warningExchange")
    public TopicExchange warningExchange() {
        return new TopicExchange(MQConstants.exchangerName,true,false);
    }

    @Bean(name = "pushQueue")
    public Queue pushQueue() {
        return new Queue(MQConstants.pushQueueName, true, false, false);
    }
    @Bean(name = "bindingPushQueue")
    public Binding bindingPushQueue() {
        return BindingBuilder.bind(pushQueue()).to(warningExchange()).with(MQConstants.pushRoutingKey);
    }

    @Bean(name = "checkQueue")
    public Queue checkQueue() {
        return new Queue(MQConstants.checkQueueName, true, false, false);
    }
    @Bean(name = "bindinCheckQueue")
    public Binding bindingCheckQueue() {
        return BindingBuilder.bind(checkQueue()).to(warningExchange()).with(MQConstants.checkRoutingKey);
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
