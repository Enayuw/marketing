package com.br.marketing.monkey.enums.ningbo;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {

    WAITING(0, "未处理"),
    RUNNING(1, "推送中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败");

    private Integer code;

    private String description;

    TaskStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}
