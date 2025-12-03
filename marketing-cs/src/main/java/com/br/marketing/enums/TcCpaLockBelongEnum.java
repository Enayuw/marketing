package com.br.marketing.enums;

/**
 * clean_status枚举
 */
public enum TcCpaLockBelongEnum {

    BELONG_BR(1,"我司"),
    BELONG_OTR(2,"友商"),
    BELON_BLANK(3,"空白组"),
    ;

    TcCpaLockBelongEnum(Integer value, String desc){
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
