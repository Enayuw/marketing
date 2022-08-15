package com.br.marketing.enums;

public enum ScoreStatusEnum {

    running(3),merge(1),push(2),offlinemerge(4);

    ScoreStatusEnum(Integer value) {
        this.value = value;
    }

    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
