package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DistributeSourceTypeEnum {
    TRANSFER("1", "转化数据");

    private String value;
    private String desc;
}
