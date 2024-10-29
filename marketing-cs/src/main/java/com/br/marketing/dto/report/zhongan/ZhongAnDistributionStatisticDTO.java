package com.br.marketing.dto.report.zhongan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 众安分组评分分布dto
 *
 * @author senyang.zheng
 * @date 2024/09/20
 */
@Data
@ApiModel(value = "众安分组评分分布")
public class ZhongAnDistributionStatisticDTO {

    @ApiModelProperty("报表id")
    private String reportId;
    @ApiModelProperty("模型字段")
    private String scoreField;
    @ApiModelProperty("模型值")
    private String scoreValue;
    @ApiModelProperty("维度")
    private String dimensionField;
    @ApiModelProperty("维度值")
    private String dimensionValue;
    @ApiModelProperty("指标名称")
    private String itemName;
    @ApiModelProperty("指标值")
    private String itemValue;

}
