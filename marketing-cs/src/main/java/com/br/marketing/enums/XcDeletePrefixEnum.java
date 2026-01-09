package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum XcDeletePrefixEnum {

    OUT(1,  "out", "timeRange范围外量级"),
    INTER_WITHOUT_CON(2, "interWithoutCon", "无条件求交量级"),
    INTER_WITH_CON(3, "interWithCon", "有条件求交量级");

    private Integer value;

    private String alias;

    private String desc;

    XcDeletePrefixEnum(Integer value, String alias, String desc) {
        this.value = value;
        this.alias = alias;
        this.desc = desc;
    }


}