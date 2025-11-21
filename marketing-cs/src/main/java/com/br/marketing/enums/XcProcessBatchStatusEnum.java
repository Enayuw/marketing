package com.br.marketing.enums;

/**
 * 报表任务状态枚举
 *
 * @author zhen.li1
 * @dateTime 2024-08-19 19:01
 */
public enum XcProcessBatchStatusEnum {

    EXECUTE_WAITED(0, "待执行"),
    EXECUTEING(1, "执行中"),
    EXECUTED(2, "执行完成");

    XcProcessBatchStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
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
