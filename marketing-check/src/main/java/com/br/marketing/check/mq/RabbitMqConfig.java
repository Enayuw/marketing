package com.br.marketing.check.mq;


import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public static final int MQ_LISTENER = 1;
    public static final int MQ_CONCURRENT_LISTENER = 5;



    /**
     * marketing 通用交换机
     *
     * @return
     */
    @Bean(name = MQConstants.MARKETINGEXCHANGER_NAME)
    public TopicExchange gateExchange() {
        return new TopicExchange(MQConstants.MARKETINGEXCHANGER_NAME, true, false);
    }


    @Bean(name = MQConstants.MARKETING_PUSH_DASS_SCORE)
    public Queue pushDassQueue() {
        return new Queue(MQConstants.MARKETING_PUSH_DASS_SCORE, true, false, false);
    }

    @Bean(name = MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_SCORE)
    public Binding bindingPushDassQueue() {
        return BindingBuilder.bind(pushDassQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_SCORE);
    }

    @Bean(name = MQConstants.MARKETING_PUSH_TWOSEVEN_FILETRANSFER)
    public Queue pushSevenQueue() {
        return new Queue(MQConstants.MARKETING_PUSH_TWOSEVEN_FILETRANSFER, true, false, false);
    }

    @Bean(name = MQConstants.ROUTING_KEY_MARKETING_PUSH_TWOSEVEN_FILETRANSFER)
    public Binding bindingSevenQueue() {
        return BindingBuilder.bind(pushSevenQueue()).to(gateExchange()).with(MQConstants.ROUTING_KEY_MARKETING_PUSH_TWOSEVEN_FILETRANSFER);
    }

    //region 海尔3.0 没有转化数据推电销
//    /**
//     * 海尔消金转电销-转化数据队列
//     */
//    @Bean(name = MQConstants.MARKETING_QUEUE_PUSH_TRANSFER_HAIER)
//    public Queue pushTransferHaierQueue() {
//        return new Queue(MQConstants.MARKETING_QUEUE_PUSH_TRANSFER_HAIER, true, false, false);
//    }
//
//    /**
//     * 海尔消金转电销-交换机绑定队列
//     */
//    @Bean(name = MQConstants.ROUTING_KEY_MARKETING_QUEUE_PUSH_TRANSFER_HAIER)
//    public Binding bindingTransferHaierQueue(@Qualifier(MQConstants.MARKETING_QUEUE_PUSH_TRANSFER_HAIER) Queue queue
//            , @Qualifier(MQConstants.MARKETINGEXCHANGER_NAME) Exchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange).with(MQConstants.ROUTING_KEY_MARKETING_QUEUE_PUSH_TRANSFER_HAIER).noargs();
//    }
//endregion

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
