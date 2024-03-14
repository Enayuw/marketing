package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DistributeSourceTypeEnum {
    TRANSFER("1", "转化数据"),
    CALL_RECORD("2", "客服拨打记录"),
    ZHONGAN_LOCKING_DATA("3", "众安锁定数据"),
    ;

    private String value;
    private String desc;
}
