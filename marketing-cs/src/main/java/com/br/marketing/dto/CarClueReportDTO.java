package com.br.marketing.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class CarClueReportDTO {
    @NotNull(message = "Page number cannot be null")
    private Integer current = 1;

    @NotNull(message = "Page size cannot be null")
    private Integer size = 10;

    private String createTimeStart;
    private String createTimeEnd;
    private String intention;
    private Integer clueDataStatus;
    private String updateTimeStart;
    private String updateTimeEnd;
    private String cluePushChannel;
    private Integer cluePushStatus;
    private String pushTimeStart;
    private String pushTimeEnd;
    // 数据入库状态
    private Integer status;
    private String callBackTimeStart;
    private String callBackTimeEnd;
}
