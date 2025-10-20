package com.br.marketing.enums;

import lombok.Getter;

/**
 * 车线索回调状态枚举
 *
 * @author zhen.li1
 * @date 2025/01/15
 */
@Getter
public enum SmsCallBackTypeEnum {

    ONHOOK(2, "挂机短信"),
    PURE(5, "纯短信");


    SmsCallBackTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;


}
