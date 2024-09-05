package com.br.marketing.service.rulecenter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BuildTypeEnum {

    HANDLE(1,"手动生成"),AUTOBUILD(2,"自动生成");

    private Integer code;

    private String value;
}
