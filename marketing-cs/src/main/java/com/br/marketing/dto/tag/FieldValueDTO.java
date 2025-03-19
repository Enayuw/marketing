package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "字段值配置DTO")
public class FieldValueDTO {
    @ApiModelProperty(value = "值")
    private String value;
    
    @ApiModelProperty(value = "显示文本")
    private String label;
} 