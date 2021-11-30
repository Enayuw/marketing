package com.br.marketing.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataTypeEnum {

    SCORE(1),ERROR(2),DIANXIAO(3),QIQI(4),TRANSFER(5);
    private Integer value;
}
