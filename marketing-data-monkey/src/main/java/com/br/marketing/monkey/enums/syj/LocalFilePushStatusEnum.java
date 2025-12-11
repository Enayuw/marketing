package com.br.marketing.monkey.enums.syj;

import lombok.Getter;

@Getter
public enum LocalFilePushStatusEnum {

    NOT_PUSHED("0", "未推送"),
    PUSHING("1", "推送中"),
    PARTIAL_SUCCESS("2", "部分成功"),
    PUSH_SUCCESS("3", "推送成功"),
    PUSH_FAILED("4", "推送失败");

    private String code;

    private String description;

    LocalFilePushStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
