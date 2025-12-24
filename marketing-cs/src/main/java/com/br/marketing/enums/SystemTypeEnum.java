package com.br.marketing.enums;

public enum SystemTypeEnum {

    MARKETING(0, "营销中台"),
    CALL(1, "外呼系统");

    private Integer code;

    private String desc;

    SystemTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
