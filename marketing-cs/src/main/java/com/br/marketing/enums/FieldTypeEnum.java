package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum FieldTypeEnum {
    STRING("STRING", "字符串"),
    NUMBER("NUMBER", "数字"),
    BOOLEAN("BOOLEAN", "布尔值"),
    DATE("DATE", "日期"),
    ENUM("ENUM", "枚举值");

    private final String code;
    private final String desc;

    FieldTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FieldTypeEnum getByCode(String code) {
        for (FieldTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
} 