package com.br.marketing.xcconsumer.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

/**
 * -----------------------------
 * PackageName： com.br.marketing.xcconsumer.consumer.rocketmq
 * ClassName：MarketingXieChengReportConsumer
 * Description：
 *
 * @author：it-yml CreateTime：2025-07-24
 * -----------------------------
 */
@Service
public class MarketingXieChengReportConsumer {

    @Service
    @RocketMQMessageListener(topic = MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_A,
            consumerGroup = MarketingXieChengConstants.GROUP_MARKETING_XIECHENG_REPORT_A,
            selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_A,
            awaitTerminationMillisWhenShutdown = 10000)
    public class MarketingXiechengReportQueueConsumerA extends AbstractXieChengReportConsumer {
        @Override
        protected String consumerName() {
            return MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_A;
        }
    }
    @Service
    @RocketMQMessageListener(topic = MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_B,
            consumerGroup = MarketingXieChengConstants.GROUP_MARKETING_XIECHENG_REPORT_B,
            selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_B,
            awaitTerminationMillisWhenShutdown = 10000)
    public class MarketingXiechengReportQueueConsumerB extends AbstractXieChengReportConsumer {
        @Override
        protected String consumerName() {
            return MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_B;
        }
    }

    @Service
    @RocketMQMessageListener(topic = MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_C,
            consumerGroup = MarketingXieChengConstants.GROUP_MARKETING_XIECHENG_REPORT_C,
            selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_C,
            awaitTerminationMillisWhenShutdown = 10000)
    public class MarketingXiechengReportQueueConsumerC extends AbstractXieChengReportConsumer {
        @Override
        protected String consumerName() {
            return MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_C;
        }
    }
    @Service
    @RocketMQMessageListener(topic = MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_D,
            consumerGroup = MarketingXieChengConstants.GROUP_MARKETING_XIECHENG_REPORT_D,
            selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_D,
            awaitTerminationMillisWhenShutdown = 10000)
    public class MarketingXiechengReportQueueConsumerD extends AbstractXieChengReportConsumer {
        @Override
        protected String consumerName() {
            return MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_D;
        }
    }

    @Service
    @RocketMQMessageListener(topic = MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_E,
            consumerGroup = MarketingXieChengConstants.GROUP_MARKETING_XIECHENG_REPORT_E,
            selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_E,
            awaitTerminationMillisWhenShutdown = 10000)
    public class MarketingXiechengReportQueueConsumerE extends AbstractXieChengReportConsumer {
        @Override
        protected String consumerName() {
            return MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_E;
        }
    }

}
