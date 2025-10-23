package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum XcReportTypeEnum {

    CALL(1, "通话明细"),

    SMS(2, "短信");

    private Integer value;

    private String desc;

    XcReportTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }


}
