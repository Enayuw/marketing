package com.br.marketing.client.rulecleaning;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 字段清洗预览DTO
 * @author guangxiu.li
 * @date 2025/5/10
 */
@Data
@ApiModel(description = "字段清洗预览请求参数")
public class FieldCleaningPreviewDTO {
    
    @ApiModelProperty(value = "字段样例数据", required = true)
    private String fieldSample;
    
    @ApiModelProperty(value = "清洗规则（JSON格式）", required = true)
    private String cleaningRule;
} 