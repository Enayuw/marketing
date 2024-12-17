package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum ThirdPartnerDataPassBackLogStatusEnum {

    PUSHING(0, "推送中"),
    PUSH_SUCCESS(1, "推送成功"),
    PUSH_FAIL(2, "推送失败");

    private Integer status;

    private String statusDesc;

    ThirdPartnerDataPassBackLogStatusEnum(Integer status, String statusDesc) {
        this.status = status;
        this.statusDesc = statusDesc;
    }
}
