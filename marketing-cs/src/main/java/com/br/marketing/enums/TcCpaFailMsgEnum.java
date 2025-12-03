package com.br.marketing.enums;

/**
 * clean_status枚举
 */
public enum TcCpaFailMsgEnum {

    FAIL_BLACK(1,"黑名单"),
    FAIL_TRANSFER(3,"已转化"),
    FAIL_NOONE(4,"无此用户"),
    FAIL_OUTLIMIT(5,"达到限额"),
    ;

    TcCpaFailMsgEnum(Integer value, String desc){
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
