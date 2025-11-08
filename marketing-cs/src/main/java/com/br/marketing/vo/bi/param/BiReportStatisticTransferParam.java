package com.br.marketing.vo.bi.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BiReportStatisticTransferParam {


    @NotNull(message = "报表类型不能为空")
    @Schema(description = "报表类型，必填字段")
    private String reportTypeName;

    @Schema(description = "统计日期")
    private String reportDate;

    @Schema(description = "客户编号")
    private String apiCode;


}
