package com.br.marketing.enums;

/**
 * 推送文件状态枚举
 */
public enum TcCpaCollidingTaskStatusEnum {

    STATUS_WAIT_STA(1, "待统计"),
    STATUS_STA_COMPLETED(2, "统计完成"),
    STATUS_FILTERING(3, "筛选中"),
    STATUS_FILTER_COMPLETED(4, "筛选完成"),
    STATUS_PUSHING(5, "推送中"),
    STATUS_PUSH_COMPLETED(6, "推送完成"),
    STATUS_PUSH_FAIL(7, "推送失败"),
    ;

    TcCpaCollidingTaskStatusEnum(Integer value, String desc){
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
