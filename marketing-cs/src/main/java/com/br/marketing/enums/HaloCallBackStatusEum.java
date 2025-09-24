package com.br.marketing.enums;

import lombok.Data;

/**
 * @ClassName HaloCallBackStatusEum
 * @Author hang.zhou
 * @Date 2025/9/15
 */
public enum HaloCallBackStatusEum {

    PORCESSING(0,"处理中"),
    SYNCING(3,"同步中"),
    CALL_BACK_SUCCESS(1,"推送成功"),
    CALL_BACK_FAILURE(2,"推送失败");

    private Integer code;
    private String desc;

    HaloCallBackStatusEum(Integer code, String desc) {
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
