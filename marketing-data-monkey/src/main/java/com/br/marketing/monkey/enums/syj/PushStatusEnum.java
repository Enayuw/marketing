package com.br.marketing.monkey.enums.syj;

import lombok.Getter;

@Getter
public enum PushStatusEnum {

    NO_PUSHED(0, "未推送"),
    PUSHING(1, "推送中"),
    PUSH_SUCCESS(2, "推送成功"),
    PUSH_FAILED(3, "推送失败");

    private Integer code;

    private String description;

    PushStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}
