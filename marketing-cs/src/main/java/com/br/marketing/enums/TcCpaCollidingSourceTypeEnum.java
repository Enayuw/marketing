package com.br.marketing.enums;

/**
 * colliding_source_type枚举
 */
public enum TcCpaCollidingSourceTypeEnum {

    SUCCESS(1,"撞库成功"),
    FAIL(2,"撞库失败"),
    ;
    TcCpaCollidingSourceTypeEnum(Integer value, String desc){
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
