package com.br.marketing.dto.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 区间范围配置DTO
 *
 * @author system
 * @date 2025-01-07
 */
@Data
@ApiModel("区间范围配置DTO")
public class IntervalRangeDTO {

    @ApiModelProperty("最小值")
    private Double min;

    @ApiModelProperty("最大值")
    private Double max;

    @ApiModelProperty("是否包含最小值")
    private Boolean minInclusive;

    @ApiModelProperty("是否包含最大值")
    private Boolean maxInclusive;

    @ApiModelProperty("区间显示文本")
    private String text;

    /**
     * 构造区间显示文本
     */
    public String getText() {
        if (text != null && !text.isEmpty()) {
            return text;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(minInclusive ? "[" : "(");
        sb.append(min);
        sb.append(",");
        sb.append(max);
        sb.append(maxInclusive ? "]" : ")");
        return sb.toString();
    }
}