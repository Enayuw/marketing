package com.br.marketing.dto;

import com.br.marketing.entity.ScoreRuleConfig;
import lombok.Data;

@Data
public class CustomerScoreRuleDTO extends ScoreRuleConfig {
    private Long mid;
    private String conditionInfo;
    private String apiCode;
    private String cid;

}
