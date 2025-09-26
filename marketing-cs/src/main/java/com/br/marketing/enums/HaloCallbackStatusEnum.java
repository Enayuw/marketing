package com.br.marketing.enums;

import lombok.Data;

public enum HaloCallbackStatusEnum {
    PENDING(0, "未回调"),
    SUCCESS(1, "回调成功"),
    FAIL(2, "回调失败");


    HaloCallbackStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private Integer code;

    private String desc;

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
