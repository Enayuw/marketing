package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一管理线程池名称枚举类
 */
@Getter
@AllArgsConstructor
public enum ThreadPoolNameEnum {

    // marketing-ai-mq-consumer
    AI_CONSUMER_PREUSER(1,"aiConsumerPreUser", "ai上传数据消费"),

    // marketing-ai-data-push-down
    AI_CONSUMER_UNIVERSAL_TRANSFER(2, "aiConsumerUniversalTransfer", "ai推送下游数据消费"),
    ;

    private Integer order;
    private String name;
    private String desc;
}
