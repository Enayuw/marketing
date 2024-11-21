package com.br.marketing.vo.bi.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BiReportStatisticTransferParam {


    @NotNull(message = "报表类型不能为空")
    @ApiModelProperty(value = "报表类型，必填字段")
    private String reportTypeName;

    @NotNull(message = "报表日期不能为空")
    @ApiModelProperty(value = "统计日期")
    private String reportDate;




}
