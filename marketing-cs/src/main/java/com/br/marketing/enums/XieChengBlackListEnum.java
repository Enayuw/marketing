package com.br.marketing.enums;

import com.br.marketing.common.utils.StringUtils;

/**
* @Description:携程黑名单枚举类型
* @Author: Ethan.Kang
*/
public enum XieChengBlackListEnum {
    PUBLIC_BLACKLISTS(0,"公共黑名单"),
    SELF_DEVELOPED_AI_BUSINESS_BLACKLIST(1,"自研AI业务黑名单"),
    BAIYING_BUSINESS_BLACKLIST(2,"百应业务黑名单");

    XieChengBlackListEnum(Integer value, String desc) {
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

    public static Integer getValueByDesc(String desc) {
        if (StringUtils.isEmpty(desc)) {
            return null;
        }
        for (XieChengBlackListEnum  xieChengBlackListEnum: XieChengBlackListEnum.values()) {
            if (xieChengBlackListEnum.getDesc().equals(desc)) {
                return xieChengBlackListEnum.value;
            }
        }
        return null;
    }
}
