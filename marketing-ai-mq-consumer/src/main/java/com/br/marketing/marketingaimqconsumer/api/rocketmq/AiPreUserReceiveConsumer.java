package com.br.marketing.marketingaimqconsumer.api.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.common.enums.rocketmq.AiPreUserReceiveEnum;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Service;

import static com.br.marketing.common.constants.rocketmq.AiRocketMQConstants.GROUP_MARKETING_AI_PREUSER_RECEIVE_1;
import static com.br.marketing.common.constants.rocketmq.AiRocketMQConstants.GROUP_MARKETING_AI_PREUSER_RECEIVE_2;
import static com.br.marketing.common.constants.rocketmq.AiRocketMQConstants.GROUP_MARKETING_AI_PREUSER_RECEIVE_3;
import static com.br.marketing.common.constants.rocketmq.AiRocketMQConstants.TAG_MARKETING_AI_PREUSER_RECEIVE_1;
import static com.br.marketing.common.constants.rocketmq.AiRocketMQConstants.TAG_MARKETING_AI_PREUSER_RECEIVE_2;
import static com.br.marketing.common.constants.rocketmq.AiRocketMQConstants.TAG_MARKETING_AI_PREUSER_RECEIVE_3;
import static com.br.marketing.common.constants.rocketmq.AiRocketMQConstants.TOPIC_MARKETING_AI_PREUSER_RECEIVE_1;
import static com.br.marketing.common.constants.rocketmq.AiRocketMQConstants.TOPIC_MARKETING_AI_PREUSER_RECEIVE_2;
import static com.br.marketing.common.constants.rocketmq.AiRocketMQConstants.TOPIC_MARKETING_AI_PREUSER_RECEIVE_3;

@Service
public class AiPreUserReceiveConsumer {

    @Service
    @RocketMQMessageListener(topic = TOPIC_MARKETING_AI_PREUSER_RECEIVE_1,
            consumerGroup = GROUP_MARKETING_AI_PREUSER_RECEIVE_1,
            selectorExpression = TAG_MARKETING_AI_PREUSER_RECEIVE_1,
            awaitTerminationMillisWhenShutdown = 10000)
    public class AiPreUserReceiveConsumer1 extends AbstractAiPreUserReceiveConsumer {
        @Override
        protected String consumerName() {
            return AiPreUserReceiveEnum.Q1.name();
        }
    }
    @Service
    @RocketMQMessageListener(topic = TOPIC_MARKETING_AI_PREUSER_RECEIVE_2,
            consumerGroup = GROUP_MARKETING_AI_PREUSER_RECEIVE_2,
            selectorExpression = TAG_MARKETING_AI_PREUSER_RECEIVE_2,
            consumeThreadMax = 20, awaitTerminationMillisWhenShutdown = 10000)
    public class AiPreUserReceiveConsumer2 extends AbstractAiPreUserReceiveConsumer {
        @Override
        protected String consumerName() {
            return AiPreUserReceiveEnum.Q2.name();
        }
    }

    @Service
    @RocketMQMessageListener(topic = TOPIC_MARKETING_AI_PREUSER_RECEIVE_3,
            consumerGroup = GROUP_MARKETING_AI_PREUSER_RECEIVE_3,
            selectorExpression = TAG_MARKETING_AI_PREUSER_RECEIVE_3,
            consumeThreadMax = 20, awaitTerminationMillisWhenShutdown = 10000)
    public class AiPreUserReceiveConsumer3 extends AbstractAiPreUserReceiveConsumer {
        @Override
        protected String consumerName() {
            return AiPreUserReceiveEnum.Q3.name();
        }
    }
}
