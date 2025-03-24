package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * 标签字段配置DTO
 */
@Data
@ApiModel(description = "标签字段配置DTO")
public class TagFieldConfigDTO {
    @ApiModelProperty(value = "数据源编码")
    private String sourceCode;

    @ApiModelProperty(value = "字段编码")
    private String fieldCode;

    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    @ApiModelProperty(value = "字段值操作")
    private String fieldOption;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "所属分类编码")
    private String categoryCode;

    @ApiModelProperty(value = "所属分类名称")
    private String categoryName;

    @ApiModelProperty(value = "支持的操作符列表")
    private List<OperatorConfigDTO> operators;

    @ApiModelProperty(value = "字段可选值列表（枚举类型时有值）")
    private List<String> valueOptions;

    @ApiModelProperty(value = "是否支持子条件")
    private Boolean supportSubCondition;

    @ApiModelProperty(value = "是否支持计算操作")
    private Boolean supportCalc;

    @ApiModelProperty(value = "支持的计算单位")
    private List<String> calcUnits;

    @ApiModelProperty(value = "日期格式（日期类型时有值）")
    private String dateFormat;

    @ApiModelProperty(value = "最小值（数字类型时有值）")
    private String minValue;

    @ApiModelProperty(value = "最大值（数字类型时有值）")
    private String maxValue;

    @ApiModelProperty(value = "操作类型：input/select/datePicker")
    private String operationType;
}


