package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DistributeTypeEnum {
    CUSTOMERTRANSFER(1, "客服转化");

    private Integer value;
    private String desc;
}
