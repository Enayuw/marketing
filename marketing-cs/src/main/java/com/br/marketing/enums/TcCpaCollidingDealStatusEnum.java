package com.br.marketing.enums;

/**
 * colliding_data_deal_status枚举
 */
public enum TcCpaCollidingDealStatusEnum {

    DEAL_NO(0,"未处理"),
    DEAL_MIDDLE(1,"处理中"),
    DEAL_SUCCESS(2,"处理完成"),
    DEAL_FAIL(3,"处理异常"),
    NO_FILE(4,"文件不存在");
    TcCpaCollidingDealStatusEnum(Integer value, String desc){
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
