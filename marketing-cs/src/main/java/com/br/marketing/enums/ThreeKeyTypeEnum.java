package com.br.marketing.enums;

/**
 * 三要素的数据类型枚举
 */
public enum ThreeKeyTypeEnum {

    CELL("cell"),NAME("name"),ID("id");

    ThreeKeyTypeEnum(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
