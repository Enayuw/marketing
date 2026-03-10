package com.br.marketing.enums;

/**
 * colliding_data_deal_status枚举
 */
public enum XyfReceiveStatusEnum {

    RECEIVE_NODEC(0,"未解密"),
    RECEIVE_NOSIGN(1,"未验签"),
    RECEIVE_NOFILL(2,"未必填"),
    RECEIVE_SUCCESS(3,"接收成功");

    XyfReceiveStatusEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }
    private int code;

    private String desc;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
