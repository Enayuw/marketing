package com.br.marketing.dto;

import com.br.marketing.vo.BaseHeadConfigVO;
import lombok.Data;

@Data
public class StrategyOfGroupDTO {
    /**
     * 场景
     */
    private String groupType;

    /**
     * 场景简拼
     */
    private String groupTypeShort;

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

    /**
     * 周期结束时间
     */
    private String cycleEndDay;

    private String baseInfo;

    private BaseHeadConfigVO configVO;
}
