package com.br.marketing.common.enums;

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
    MARKETING_AI_PREUSER_RECEIVE("Marketing.Ai.PreUser.Receive", "MARKETING_AI_PREUSER_RECEIVE",
            ImmutableMap.of("Marketing.Ai.PreUser.Receive.1", "MARKETING_AI_PREUSER_RECEIVE_1", "Marketing.Ai.PreUser.Receive.2",
                    "MARKETING_AI_PREUSER_RECEIVE_2"), "AI上传数据队列"),

    MARKETING_AI_UNIVERSAL_RECEIVE("Marketing.Ai.Universal.Receive", "MARKETING_AI_UNIVERSAL_RECEIVE",
            ImmutableMap.of("Marketing.Ai.Universal.Receive", "MARKETING_AI_UNIVERSAL_RECEIVE_1", "Marketing.Ai.Universal.Receive.2",
                    "MARKETING_AI_UNIVERSAL_RECEIVE_2"), "AI推送下游通用队列");


    private final String route_key;
    private final String default_queue;
    private final Map<String, String> standby_queue;
    private final String desc;
}
