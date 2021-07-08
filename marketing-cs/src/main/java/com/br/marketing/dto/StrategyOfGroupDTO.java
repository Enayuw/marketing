package com.br.marketing.dto;

import lombok.Data;

@Data
public class StrategyOfGroupDTO {
    /**
     * 场景
     */
    private String groupType;

    /**
     * 策略
     */
    private String strategyId;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 任务执行策略 1-一次性全量；2-周期性全量
     */
    private Integer execType;

    /**
     * 周期天数
     */
    private Integer cycleDay;
}
