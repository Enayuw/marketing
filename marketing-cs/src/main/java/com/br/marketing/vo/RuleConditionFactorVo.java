package com.br.marketing.vo;

import lombok.Data;

@Data
public class RuleConditionFactorVo {
    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段value
     */
    private String fieldValue;

    /**
     * 运算符
     */
    private String operation;
}
