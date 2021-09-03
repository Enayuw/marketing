package com.br.marketing.vo;

import com.br.marketing.entity.SoleRuleConfig;
import lombok.Data;

@Data
public class CustomerSoleRuleVO extends SoleRuleConfig {
    private String apiCode;
    private String conditionInfo;
    private String conditionDbDesc;
}
