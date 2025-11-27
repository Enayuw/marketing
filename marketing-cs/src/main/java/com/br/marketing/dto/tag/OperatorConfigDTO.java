package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "操作符配置DTO")
public class OperatorConfigDTO {
    @Schema(description = "操作符编码")
    private String code;
    
    @Schema(description = "操作符名称")
    private String name;
} 