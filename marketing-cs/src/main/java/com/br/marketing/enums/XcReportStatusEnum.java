package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum XcReportStatusEnum {

    SUCCESS(1, "成功"),

    FAIL(2, "失败");

    private Integer value;

    private String desc;

    XcReportStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }


}
