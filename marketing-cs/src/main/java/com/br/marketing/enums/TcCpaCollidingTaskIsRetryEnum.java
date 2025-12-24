package com.br.marketing.enums;

/**
 * 是否重试枚举
 */
public enum TcCpaCollidingTaskIsRetryEnum {

    RETRY_NO(0, "非重试"),
    RETRY_YES(1, "重试"),
    ;

    TcCpaCollidingTaskIsRetryEnum(Integer value, String desc){
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
