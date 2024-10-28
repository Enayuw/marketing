package com.br.marketing.enums;

public enum ConditionTypeEnum {

    RUNNING(1,"自动")
    ,MERGE(2,"手动")
    ;

    ConditionTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc=desc;
    }

    private Integer value;

    private String desc;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
