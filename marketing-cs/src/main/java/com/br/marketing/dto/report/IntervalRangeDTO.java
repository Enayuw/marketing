package com.br.marketing.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 区间范围配置DTO
 *
 * @author bingxu.kong
 * @date 2025-01-07
 */
@Data
@Schema(description = "区间范围配置DTO")
public class IntervalRangeDTO {

    @Schema(description = "最小值")
    @JsonProperty("min")
    private Double min;

    @Schema(description = "最大值")
    @JsonProperty("max")
    private Double max;

    @Schema(description = "是否包含最小值")
    @JsonProperty("minInclusive")
    private Boolean minInclusive;

    @Schema(description = "是否包含最大值")
    @JsonProperty("maxInclusive")
    private Boolean maxInclusive;

    @Schema(description = "区间显示文本")
    @JsonProperty("text")
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