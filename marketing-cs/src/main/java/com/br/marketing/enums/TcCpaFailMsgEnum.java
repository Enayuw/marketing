package com.br.marketing.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * clean_status枚举
 */
public enum TcCpaFailMsgEnum {

    FAIL_BLACK(1,"黑名单", null),
    FAIL_LOCK_BY_OTR(2,"被友商锁定", 2),
    FAIL_TRANSFER(3,"已转化", null),
    FAIL_NOONE(4,"无此用户", null),
    FAIL_OUTLIMIT(5,"达到限额", null),
    FAIL_BLANK(6,"空白组", 3),
    ;

    private static final Map<Integer, TcCpaFailMsgEnum> ENUM_MAP = new HashMap<>();

    static {
        for (TcCpaFailMsgEnum value : TcCpaFailMsgEnum.values()) {
            ENUM_MAP.put(value.value, value);
            ENUM_MAP.put(value.value, value);
        }
    }

    public static TcCpaFailMsgEnum getByValue(Integer value) {
        return ENUM_MAP.get(value);
    }

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
