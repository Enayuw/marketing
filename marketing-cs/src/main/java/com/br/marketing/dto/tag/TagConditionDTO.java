package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 标签条件DTO
 */
@Data
public class TagConditionDTO {
    @NotBlank(message = "字段编码不能为空")
    @ApiModelProperty(value = "字段编码", required = true)
    private String fieldCode;

    @NotBlank(message = "字段名称不能为空")
    @ApiModelProperty(value = "字段名称", required = true)
    private String fieldName;

    @NotBlank(message = "操作符不能为空")
    @ApiModelProperty(value = "操作符（=, !=, >, <等）", required = true)
    private String operator;

    @NotBlank(message = "条件值不能为空")
    @ApiModelProperty(value = "条件值", required = true)
    private String value;

    @ApiModelProperty(value = "时间范围（外呼/回访/知识库）")
    private String timeRange;
}