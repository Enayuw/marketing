package com.br.marketing.task.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述：跑分任务批次DTO
 *
 * @author junzhe.ma
 * @date 2026-01-27 14:08
 */
@Data
public class ScoreTaskBatchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前批次对应的上一批次的最大记录ID
     */
    private Long preId;

    /**
     * 跑分条件序号
     */
    private Integer conditionIndex;

    /**
     * 分组ID
     */
    private Integer groupId;

    /**
     * 批次最小记录ID
     */
    private Long minId;

    /**
     * 批次最大记录ID
     */
    private Long maxId;

    /**
     * 未完全处理完成时当前批次待处理最小记录ID
     */
    private Long minUnCompleteId;

    /**
     * 当前页，即批次
     */
    private Long currentPage;

    /**
     * 当前批次的已处理的数据量
     */
    private Integer completeNum;
}
