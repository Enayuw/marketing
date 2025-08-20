package com.br.marketing.enums;

/**
 * colliding_data_deal_status枚举
 */
public enum TcFileDataDealStatusEnum {

    STATUS_FAIL(0,"异常"),
    STATUS_SUCCESS(1,"正常");
    TcFileDataDealStatusEnum(Integer value, String desc){
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
