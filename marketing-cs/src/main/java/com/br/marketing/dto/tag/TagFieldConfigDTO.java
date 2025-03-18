package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * 标签字段配置DTO
 */
@Data
public class TagFieldConfigDTO {
    @ApiModelProperty(value = "字段编码")
    private String fieldCode;

    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "枚举值列表")
    private List<String> enumValues;
}