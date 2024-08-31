package com.br.marketing.dto.report;

import lombok.Data;

import java.util.List;

@Data
public class ScoreReportRuleDTO {

    /**
     * 规则名称
     */
    String ruleName;

    /**
     * X轴模型
     */
    List<String>  X ;

    /**
     * Y轴模型
     */
    List<String>  Y ;

    /**
     * 优先级
     */
    Integer order ;




}
