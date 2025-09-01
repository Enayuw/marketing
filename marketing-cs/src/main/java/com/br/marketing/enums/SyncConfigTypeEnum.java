package com.br.marketing.enums;

/**
 * 推送文件状态枚举
 */
public enum SyncConfigTypeEnum {

    STATUS_USABLE(1, "可用"),
    STATUS_DEL(2, "删除");

    SyncConfigTypeEnum(Integer value, String desc){
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
