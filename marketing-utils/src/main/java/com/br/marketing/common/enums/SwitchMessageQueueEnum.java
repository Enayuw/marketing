package com.br.marketing.common.enums;

import com.br.marketing.common.utils.AiMQConstants;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * 动态切换消息队列配置枚举
 */
@Getter
@AllArgsConstructor
public enum SwitchMessageQueueEnum {
    MARKETING_AI_PREUSER_RECEIVE(AiMQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE,
            ImmutableMap.of(AiMQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE,AiMQConstants.MARKETING_AI_PREUSER_RECEIVE,
                    AiMQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_1,AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_1,
                    AiMQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE_2,AiMQConstants.MARKETING_AI_PREUSER_RECEIVE_2), "AI上传数据队列"),

    MARKETING_AI_UNIVERSAL_RECEIVE(AiMQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE,
            ImmutableMap.of(AiMQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE,AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE,
                    AiMQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_1,AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_1,
                    AiMQConstants.ROUTING_KEY_MARKETING_AI_UNIVERSAL_RECEIVE_2,AiMQConstants.MARKETING_AI_UNIVERSAL_RECEIVE_2), "AI推送下游通用队列");


    private final String default_route_key;
    private final Map<String, String> queueAndRoutingKeyMap;
    private final String desc;
}
