package com.br.marketing.enums;

public enum PushRuleStatusEnum {

    TO_BE_BUILDING(-1,"构建中"),
    TO_BE_RUNNING(0,"待执行"),
    RUNNING(1,"执行中"),
    TO_BE_CONFIRMED(2,"待确认"),
    PUSH_FAIL(3,"推送失败"),
    CONFIRMED_SUCCESS(4,"确认成功"),
    CONFIRMED_FAIL(5,"确认失败"),
    CONFIRMED_TIME_OUT(6,"超时待确认"),
    EXCEPTIONS_TO_REFILLED(7,"异常待补推"),
    EXCEPTIONS_RUNNING(8,"异常补推中");

    PushRuleStatusEnum(Integer value, String desc) {
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
