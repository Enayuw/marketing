package com.br.marketing.enums;

public enum FilterTypeEnum {

    RUNNING_SCORES(0,"跑分数据推决策"),
    CREDENTIAL_STUFFING(1,"跑分及撞库结果筛选推决策");

    FilterTypeEnum(Integer value, String desc) {
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
