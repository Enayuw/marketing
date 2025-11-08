package com.br.marketing.dto.report.zhongan;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Schema(description = "报表统计指标")
public class ReportStatisticField {

    @ApiModelProperty("报表id")
    private String reportId;
    @ApiModelProperty("fieldY")
    private String fieldY;
    @ApiModelProperty("指标名称")
    private String itemName;
    @ApiModelProperty("指标值")
    private String itemValue;

}
