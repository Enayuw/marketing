package com.br.marketing.monkey.enums.syj;

import lombok.Getter;

@Getter
public enum InvocationStatusEnum {

    INVOCATION_SUCCESS(0, "调用成功"),
    INVOCATION_ERROR(1, "系统异常");

    private Integer code;

    private String description;

    private InvocationStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}
