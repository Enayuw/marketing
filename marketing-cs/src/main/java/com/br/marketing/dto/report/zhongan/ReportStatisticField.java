package com.br.marketing.dto.report.zhongan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "报表统计指标")
public class ReportStatisticField {

    @Schema(description = "报表id")
    private String reportId;
    @Schema(description = "fieldY")
    private String fieldY;
    @Schema(description = "指标名称")
    private String itemName;
    @Schema(description = "指标值")
    private String itemValue;

}
