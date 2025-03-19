package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "操作符配置DTO")
public class OperatorConfigDTO {
    @ApiModelProperty(value = "操作符编码")
    private String code;
    
    @ApiModelProperty(value = "操作符名称")
    private String name;
} 