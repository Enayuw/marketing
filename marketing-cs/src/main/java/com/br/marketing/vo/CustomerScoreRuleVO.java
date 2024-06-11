package com.br.marketing.vo;

import com.br.marketing.entity.ScoreRuleConfig;
import lombok.Data;

@Data
public class CustomerScoreRuleVO extends ScoreRuleConfig {
    private String apiCode;
    private String startDate;
    private Integer dataLimit;
    private Integer isOrNoScoreVer;
    private Integer priority;
    // 生成方式 1：手动 2：自动
    private Integer buildType;
}
