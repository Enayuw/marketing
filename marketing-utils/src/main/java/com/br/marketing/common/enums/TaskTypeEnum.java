package com.br.marketing.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;



@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    DIRECTDATA(1,"透传不跑分"),STRATYGYDATA(0,"策略跑分"),PRODUCTDATA(2,"产品跑分");
    private Integer value;
    private String desc;

}
