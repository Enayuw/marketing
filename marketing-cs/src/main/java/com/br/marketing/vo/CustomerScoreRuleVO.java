package com.br.marketing.vo;

import com.br.marketing.entity.ScoreRuleConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class CustomerScoreRuleVO extends ScoreRuleConfig {
    private String apiCode;
    private String startDate;
    private Integer dataLimit;
    private Integer isOrNoScoreVer;
    private Integer priority;
}
