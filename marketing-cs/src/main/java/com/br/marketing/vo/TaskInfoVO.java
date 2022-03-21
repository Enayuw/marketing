package com.br.marketing.vo;

import lombok.Data;

@Data
public class TaskInfoVO {
    private String batchNumber;
    private Integer taskType;
    private Long taskId;
    private Long ruleId;
    private String extendShowTitle;
    private String strategyProductJson;
}
