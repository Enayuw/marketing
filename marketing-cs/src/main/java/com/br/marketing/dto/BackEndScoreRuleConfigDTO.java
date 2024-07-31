package com.br.marketing.dto;

import lombok.Data;

import java.util.List;


@Data
public class BackEndScoreRuleConfigDTO {

    /**
     * 规则id
     */
    private List<Long> ruleIds;

    /**
     * apiCode
     */
    private String apiCode;

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
