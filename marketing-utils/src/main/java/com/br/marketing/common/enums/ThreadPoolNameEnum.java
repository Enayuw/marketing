package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_PREUSER_RECEIVE;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_1;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_2;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_1;
import static com.br.marketing.common.utils.AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_2;

/**
 * 统一管理线程池名称枚举类
 */
@Getter
@AllArgsConstructor
public enum ThreadPoolNameEnum {

    // marketing-ai-mq-consumer
    AI_PREUSER_RECEIVE(1,MARKETING_AI_PREUSER_RECEIVE, "ai上传数据队列消费"),
    AI_PREUSER_RECEIVE_1(2,MARKETING_AI_PREUSER_RECEIVE_1, "ai上传数据队列消费备用1"),
    AI_PREUSER_RECEIVE_2(3,MARKETING_AI_PREUSER_RECEIVE_2, "ai上传数据队列消费备用2"),

    // marketing-ai-data-push-down
    AI_UNIVERSAL_RECEIVE(4, MARKETING_AI_UNIVERSAL_RECEIVE, "ai推送下游数据队列消费"),
    AI_UNIVERSAL_RECEIVE_1(5, MARKETING_AI_UNIVERSAL_RECEIVE_1, "ai推送下游数据队列消费备用1"),
    AI_UNIVERSAL_RECEIVE_2(6, MARKETING_AI_UNIVERSAL_RECEIVE_2, "ai推送下游数据队列消费备用2"),
    ;

    private Integer order;
    private String name;
    private String desc;
}
