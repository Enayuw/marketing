package com.br.marketing.enums;

/**
 * @ClassName ErrorMarkTypeEnum
 * @Description TODO
 * @Author kongbx
 * @Date 2025/1/8 18:56
 */
public enum ErrorMarkTypeEnum {
    ES_ERROR(0,"查询ES异常"),
    POLICY_ERROR(1,"推送决策异常"),
    HALO_CALLBACK_ERROR(2,"哈啰回调异常");

    ErrorMarkTypeEnum(Integer value, String desc) {
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
