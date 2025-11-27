package com.br.marketing.dto.report.zhongan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 众安分组评分分布dto
 *
 * @author senyang.zheng
 * @date 2024/09/20
 */
@Data
@Schema(description = "众安分组评分分布")
public class ZhongAnDistributionStatisticDTO {

    @Schema(description = "报表id")
    private String reportId;
    @Schema(description = "模型字段")
    private String scoreField;
    @Schema(description = "模型值")
    private String scoreValue;
    @Schema(description = "维度")
    private String dimensionField;
    @Schema(description = "维度值")
    private String dimensionValue;
    @Schema(description = "指标名称")
    private String itemName;
    @Schema(description = "指标值")
    private String itemValue;

}
