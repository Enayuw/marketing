package com.br.marketing.enums;

/**
 * supplyType枚举
 */
public enum TcCpaSupplyTypeEnum {

    SUPPLY_COMMON(1,"通用"),
    SUPPLY_CUS(1,"定制"),
    ;

    TcCpaSupplyTypeEnum(Integer value, String desc){
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
