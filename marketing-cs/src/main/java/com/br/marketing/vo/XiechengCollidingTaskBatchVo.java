package com.br.marketing.vo;

import com.br.marketing.entity.XiechengCollidingTaskBatch;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class XiechengCollidingTaskBatchVo extends XiechengCollidingTaskBatch {

    /**
     * 预估量级执行条件
     */
    private String taskExecutionConditions;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    private LocalDateTime releaseTimeBegin;

    private LocalDateTime releaseTimeEnd;
}
