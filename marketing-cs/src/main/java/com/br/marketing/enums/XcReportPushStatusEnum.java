package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum XcReportPushStatusEnum {

    WAITED(1, "未推送"),

    PUSHED(2, "已推送"),

    PUSH_FAIL(3, "推送异常");

    private Integer value;

    private String desc;

    XcReportPushStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }


}
