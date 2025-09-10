package com.br.marketing.common.enums.rocketmq;

import com.br.marketing.common.constants.rocketmq.AiRocketMQConstants;
import lombok.Getter;

@Getter
public enum AiPreUserReceiveEnum implements LoadBalanceQueue{
    Q1(AiRocketMQConstants.TOPIC_MARKETING_AI_PREUSER_RECEIVE_1,
            AiRocketMQConstants.TAG_MARKETING_AI_PREUSER_RECEIVE_1),

    Q2(AiRocketMQConstants.TOPIC_MARKETING_AI_PREUSER_RECEIVE_2,
            AiRocketMQConstants.TAG_MARKETING_AI_PREUSER_RECEIVE_2),

    Q3(AiRocketMQConstants.TOPIC_MARKETING_AI_PREUSER_RECEIVE_3,
            AiRocketMQConstants.TAG_MARKETING_AI_PREUSER_RECEIVE_3),
    ;

    private final String topic;
    private final String tag;

    AiPreUserReceiveEnum(String topic, String tag) {
        this.topic = topic;
        this.tag = tag;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getTag() {
        return tag;
    }
}
