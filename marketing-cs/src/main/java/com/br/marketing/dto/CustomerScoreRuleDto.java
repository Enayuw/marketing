package com.br.marketing.dto;

import com.br.marketing.entity.ScoreRuleConfig;
import lombok.Data;

@Data
public class CustomerScoreRuleDto extends ScoreRuleConfig {
    private String cid;
    private String apiCode;
}
