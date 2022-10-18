package com.br.marketing.enums;

public enum ZkScoreStatusEnum {

    RUNNING("跑分中")
    ,PAUSE("暂停中");

    ZkScoreStatusEnum(String value) {
        this.value=value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
