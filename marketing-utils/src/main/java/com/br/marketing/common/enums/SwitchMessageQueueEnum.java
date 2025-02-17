package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 动态切换消息队列配置枚举
 */
@Getter
@AllArgsConstructor
public enum SwitchMessageQueueEnum {
    MARKETING_AI_PREUSER_RECEIVE("Marketing.Ai.PreUser.Receive", "MARKETING_AI_PREUSER_RECEIVE",
            new String[]{"MARKETING_AI_PREUSER_RECEIVE_1", "MARKETING_AI_PREUSER_RECEIVE_2"}, "AI上传数据队列"),

    MARKETING_AI_UNIVERSAL_RECEIVE("Marketing.Ai.Universal.Receive", "MARKETING_AI_UNIVERSAL_RECEIVE",
            new String[]{"MARKETING_AI_UNIVERSAL_RECEIVE_1", "MARKETING_AI_UNIVERSAL_RECEIVE_2"}, "AI推送下游通用队列");


    private final String route_key;
    private final String default_queue;
    private final String[] standby_queue;
    private final String desc;
}
