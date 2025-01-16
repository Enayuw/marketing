package com.br.marketing.enums;

public enum RetryStatusEnum {

    AWAIT_COMPLETE(0,"待补推"),
    PUSH_COMPLETE(1,"补推完成");

    RetryStatusEnum(Integer value, String desc) {
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
