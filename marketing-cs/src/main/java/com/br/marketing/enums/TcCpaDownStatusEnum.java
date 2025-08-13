package com.br.marketing.enums;

/**
 * down_status枚举
 */
public enum TcCpaDownStatusEnum {

    DEAL_NO(0,"未完成"),
    DEAL_SUCCESS(1,"完成"),
    DEAL_FAIL(2,"失败");
    TcCpaDownStatusEnum(Integer value, String desc){
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
