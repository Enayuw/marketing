package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModel;
import lombok.Data;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModel;

@Data
@ApiModel(description = "操作符配置DTO")
public class OperatorConfigDTO {
    @Schema(description = "操作符编码")
    private String code;
    
    @Schema(description = "操作符名称")
    private String name;
} 