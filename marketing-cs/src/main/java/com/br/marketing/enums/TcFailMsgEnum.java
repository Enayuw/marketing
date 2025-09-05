package com.br.marketing.enums;

/**
 * colliding_data_deal_status枚举
 */
public enum TcFailMsgEnum {

    FAILMSG_BLACK("1","黑名单"),
    FAILMSG_LOCKED("2","已被锁定"),
    FAILMSG_TRANSFER("3","已转化"),
    FAILMSG_NO_USERKEY("4","无此用户");
    TcFailMsgEnum(String value, String desc){
        this.value = value;
        this.desc=desc;
    }
    private String value;

    private String desc;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
