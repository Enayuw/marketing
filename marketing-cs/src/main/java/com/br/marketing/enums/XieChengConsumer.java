package com.br.marketing.enums;

import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import lombok.Data;

/**
 * -----------------------------
 * PackageName： com.br.marketing.enums
 * ClassName：XieChengConsumer
 * Description：携程消费端枚举
 *
 * @author：it-yml CreateTime：2025-07-24
 * -----------------------------
 */
public enum XieChengConsumer {
    A("consumer_A", MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_A, MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_A),
    B("consumer_B", MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_B, MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_B),
    C("consumer_C", MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_C, MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_C),
    D("consumer_D", MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_D, MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_D),
    E("consumer_E", MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_E, MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_E);

    private final String consumerName;
    private final String topic;
    private final String tag;

    XieChengConsumer(String consumerName, String topic, String tag) {
        this.consumerName = consumerName;
        this.topic = topic;
        this.tag = tag;
    }

    // 添加getter方法
    public String getConsumerName() {
        return consumerName;
    }

    public String getTopic() {
        return topic;
    }

    public String getTag() {
        return tag;
    }

    public static XieChengConsumer fromName(String name) {
        for (XieChengConsumer consumer : values()) {
            if (consumer.consumerName.equals(name)) {
                return consumer;
            }
        }
        return A;
    }
}
