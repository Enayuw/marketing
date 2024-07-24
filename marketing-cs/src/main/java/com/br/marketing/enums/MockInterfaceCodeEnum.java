package com.br.marketing.enums;

public enum MockInterfaceCodeEnum {

    ITF_WUBA_01("ITF_WUBA_01",""),
    ITF_WUBA_02("ITF_WUBA_02",""),
    ITF_WUBA_03("ITF_WUBA_03",""),
    ITF_WUBA_04("ITF_WUBA_04",""),
    ;

    MockInterfaceCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
