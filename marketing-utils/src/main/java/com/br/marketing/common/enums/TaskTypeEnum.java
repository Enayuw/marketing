package com.br.marketing.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    DIRECTDATA(0,"透传不跑分"),STRATYGYDATA(1,"策略跑分"),PRODUCTDATA(2,"产品跑分");
    private Integer value;
    private String desc;

}
