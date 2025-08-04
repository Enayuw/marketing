package com.br.marketing.enums.rulecenter;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RuleCenterPushTargetEnum {

    PUSH_POLICY(0, "推送决策"),
    ORIGINAL_INTERFACE(1, "数据打标");

    private Integer code;
    private String desc;


}
