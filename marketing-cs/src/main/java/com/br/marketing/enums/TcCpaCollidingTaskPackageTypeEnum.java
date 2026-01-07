package com.br.marketing.enums;

/**
 * package_type枚举
 */
public enum TcCpaCollidingTaskPackageTypeEnum {

    SCORE(1,"跑分"),
    SUPPLY(2,"补充"),
    ;
    TcCpaCollidingTaskPackageTypeEnum(Integer value, String desc){
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
