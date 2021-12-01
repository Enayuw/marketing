package com.br.marketing.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataTypeEnum {

    SCORE(1,"跑分文件"),ERROR(2,"错误文件"),DIANXIAO(3,"电销"),QIQI(4,"七七撞库"),TRANSFER(5,"转化文件");
    private Integer value;
    private String desc;
}
