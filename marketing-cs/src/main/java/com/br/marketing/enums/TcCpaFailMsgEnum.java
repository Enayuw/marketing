package com.br.marketing.enums;

/**
 * clean_status枚举
 */
public enum TcCpaFailMsgEnum {

    FAIL_BLACK(1,"黑名单", null),
    FAIL_LOCK_BY_OTR(2,"被友商锁定", 2),
    FAIL_TRANSFER(3,"已转化", null),
    FAIL_NOONE(4,"无此用户", null),
    FAIL_OUTLIMIT(5,"达到限额", null),
    FAIL_BLANK(6,"达到限额", 3),
    ;

    TcCpaFailMsgEnum(Integer value, String desc, Integer lockValue){
          this.value = value;
          this.desc = desc;
          this.lockValue = lockValue;
    }
    private Integer value;

    private String desc;

    private Integer lockValue;

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

    public Integer getLockValue() {
        return lockValue;
    }

    public void setLockValue(Integer lockValue) {
        this.lockValue = lockValue;
    }
}
