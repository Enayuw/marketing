package com.br.marketing.vo;

import lombok.Data;

import java.util.List;

@Data
public class RuleConditionVo {
    /**
     * 逻辑运算符
     */
     private String logicalOperation;
     private List<RuleConditionFactorVo> operationFactor;
}
