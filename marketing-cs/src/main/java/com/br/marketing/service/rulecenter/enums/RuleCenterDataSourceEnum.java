package com.br.marketing.service.rulecenter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RuleCenterDataSourceEnum {
    DEFAULT(0, "无数据源"), SCORE(1, "跑分"), TRANSFER_VALID_ZHONGAN(2, "转化有效众安");

    private Integer code;
    private String desc;
}
