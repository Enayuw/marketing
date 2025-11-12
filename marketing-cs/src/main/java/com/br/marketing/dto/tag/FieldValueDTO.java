package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字段值配置DTO")
public class FieldValueDTO {
    @Schema(description = "值")
    private String value;
    
    @Schema(description = "显示文本")
    private String label;
} 