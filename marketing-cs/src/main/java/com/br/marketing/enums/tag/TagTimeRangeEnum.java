package com.br.marketing.enums.tag;

import lombok.Getter;

@Getter
public enum TagTimeRangeEnum {
    YESTERDAY("YESTERDAY", "昨天", 1, "d"),
    LAST_THREE_DAYS("LAST_THREE_DAYS", "最近三天", 3, "d"),
    LAST_WEEK("LAST_WEEK", "最近一周", 7, "d"),
    LAST_MONTH("LAST_MONTH", "最近一月", 1, "m"),
    LAST_THREE_MONTHS("LAST_THREE_MONTHS", "最近三月", 3, "m");

    private final String code;
    private final String desc;
    private final Integer timeNumber;
    private final String timeUnit;

    TagTimeRangeEnum(String code, String desc, Integer timeNumber, String timeUnit) {
        this.code = code;
        this.desc = desc;
        this.timeNumber = timeNumber;
        this.timeUnit = timeUnit;
    }

    public static TagTimeRangeEnum getByCode(String code) {
        for (TagTimeRangeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
} 