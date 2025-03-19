package com.br.marketing.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum OperatorEnum {
    // 数字类型操作符
    EQ("EQ", "等于", Arrays.asList(FieldTypeEnum.NUMBER.getCode(), FieldTypeEnum.ENUM.getCode(), FieldTypeEnum.BOOLEAN.getCode())),
    NE("NE", "不等于", Arrays.asList(FieldTypeEnum.NUMBER.getCode(), FieldTypeEnum.ENUM.getCode())),
    GT("GT", "大于", Arrays.asList(FieldTypeEnum.NUMBER.getCode(), FieldTypeEnum.DATE.getCode())),
    GE("GE", "大于等于", Arrays.asList(FieldTypeEnum.NUMBER.getCode(), FieldTypeEnum.DATE.getCode())),
    LT("LT", "小于", Arrays.asList(FieldTypeEnum.NUMBER.getCode(), FieldTypeEnum.DATE.getCode())),
    LE("LE", "小于等于", Arrays.asList(FieldTypeEnum.NUMBER.getCode(), FieldTypeEnum.DATE.getCode())),

    // 字符串类型操作符
    CONTAINS("CONTAINS", "包含", Arrays.asList(FieldTypeEnum.STRING.getCode(), FieldTypeEnum.ENUM.getCode())),
    NOT_CONTAINS("NOT_CONTAINS", "不包含", Arrays.asList(FieldTypeEnum.STRING.getCode(), FieldTypeEnum.ENUM.getCode())),
    
    // 日期类型操作符
    BETWEEN("BETWEEN", "介于", Arrays.asList(FieldTypeEnum.DATE.getCode(), FieldTypeEnum.NUMBER.getCode()));

    private final String code;
    private final String desc;
    private final List<String> supportedTypes;

    OperatorEnum(String code, String desc, List<String> supportedTypes) {
        this.code = code;
        this.desc = desc;
        this.supportedTypes = supportedTypes;
    }

    public static OperatorEnum getByCode(String code) {
        for (OperatorEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static List<OperatorEnum> getByFieldType(String fieldType) {
        return Arrays.stream(values())
                .filter(op -> op.getSupportedTypes().contains(fieldType))
                .collect(Collectors.toList());
    }
} 