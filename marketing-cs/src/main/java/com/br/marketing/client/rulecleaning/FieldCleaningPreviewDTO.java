package com.br.marketing.client.rulecleaning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字段清洗预览DTO
 * @author guangxiu.li
 * @date 2025/5/10
 */
@Data
@Schema(description = "字段清洗预览请求参数")
public class FieldCleaningPreviewDTO {
    
    @Schema(description = "字段样例数据", required = true)
    private String fieldSample;
    
    @Schema(description = "清洗规则（JSON格式）", required = true)
    private String cleaningRule;
} 