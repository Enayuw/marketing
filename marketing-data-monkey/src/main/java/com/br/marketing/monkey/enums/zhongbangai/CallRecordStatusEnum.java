package com.br.marketing.monkey.enums.zhongbangai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CallRecordStatusEnum {

    READY(0, "待推送"),
    RUNNING(1, "推送中"),
    SUCCESS(2, "推送成功"),
    PUSH_ERROR(3, "推送异常");

    private Integer code;
    private String desc;
}
