package com.br.marketing.enums;

/**
 * 推送文件状态枚举
 */
public enum TcCpaPushFileScriptPriorityEnum {

    PRIORITY_TIDB(1, "tidb"),
    PRIORITY_DORIS(2, "doris");

    TcCpaPushFileScriptPriorityEnum(Integer value, String desc){
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
