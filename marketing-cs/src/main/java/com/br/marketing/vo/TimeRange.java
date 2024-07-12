package com.br.marketing.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TimeRange {

    public TimeRange(String taskId, String startDate, String endDate) {
        this.taskId = taskId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 批次号
     */
    private String taskId;

    /**
     * 起始日期
     */
    private String startDate;

    /**
     * 截至日期
     */
    private String endDate;
}
