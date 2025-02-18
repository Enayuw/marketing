package com.br.marketing.service.carclue.clueenums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 渠道商匹配枚举
 */
@AllArgsConstructor
@Getter
public enum ChannelConfigTypeEnum {

    MATCH_CONFIG(1, "匹配配置"),
    PUSH_CONFIG(2, "推送配置"),
    CALLBACK_CONFIG(3, "回调配置");

    private Integer value;
    private String desc;
}
