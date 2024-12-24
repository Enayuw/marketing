package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum ThirdPartnerDataPassBackTaskPushStatusEnum {

    WAITED_EXECUTE(0, "待执行"),
    EXECUTING(1, "执行中"),
    FINISHED(2, "已完成");

    private Integer pushStatus;

    private String pushStatusDesc;

    ThirdPartnerDataPassBackTaskPushStatusEnum(Integer pushStatus, String pushStatusDesc) {
        this.pushStatus = pushStatus;
        this.pushStatusDesc = pushStatusDesc;
    }
}
