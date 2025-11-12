package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 标签字段配置DTO
 */
@Data
@Schema(description = "标签字段配置DTO")
public class TagFieldConfigDTO {
    @Schema(description = "数据源编码")
    private String sourceCode;

    @Schema(description = "字段编码")
    private String fieldCode;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "字段值操作")
    private String fieldOption;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "所属分类编码")
    private String categoryCode;

    @Schema(description = "所属分类名称")
    private String categoryName;

    @Schema(description = "支持的操作符列表")
    private List<OperatorConfigDTO> operators;

    @Schema(description = "字段可选值列表（枚举类型时有值）")
    private List<String> valueOptions;

    @Schema(description = "是否支持子条件")
    private Boolean supportSubCondition;

    @Schema(description = "是否支持计算操作")
    private Boolean supportCalc;

    @Schema(description = "支持的计算单位")
    private List<String> calcUnits;

    @Schema(description = "日期格式（日期类型时有值）")
    private String dateFormat;

    @Schema(description = "最小值（数字类型时有值）")
    private String minValue;

    @Schema(description = "最大值（数字类型时有值）")
    private String maxValue;

    @Schema(description = "操作类型：input/select/datePicker")
    private String operationType;
}


