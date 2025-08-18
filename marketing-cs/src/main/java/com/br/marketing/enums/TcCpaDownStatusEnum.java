package com.br.marketing.enums;

/**
 * down_status枚举
 */
public enum TcCpaDownStatusEnum {

    DEAL_NO(0,"未下载"),
    DEALING(1,"处理中"),
    DEAL_SUCCESS(2,"处理成功"),
    DEAL_FAIL(3,"处理失败");

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
