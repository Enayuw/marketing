package com.br.marketing.vo;

import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.SoleRuleConfig;
import lombok.Data;

@Data
public class CustomerScoreRuleVO extends ScoreRuleConfig {
    private String apiCode;
}
