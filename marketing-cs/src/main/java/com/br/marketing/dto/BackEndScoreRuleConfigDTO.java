package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
public class BackEndScoreRuleConfigDTO {

    /**
     * 规则id
     */
    private List<Long> ruleIds;

    /**
     * 跑分日期
     */
    private String startDate;

    /**
     * 跑分时间
     */
    private String taskTime;

    /**
     * 跑分数据范围
     */
    private String conditionInfo;
}
