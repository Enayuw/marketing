package com.br.marketing.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModel;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;

/**
 * 区间范围配置DTO
 *
 * @author bingxu.kong
 * @date 2025-01-07
 */
@Data
@ApiModel("区间范围配置DTO")
public class IntervalRangeDTO {

    @ApiModelProperty("最小值")
    @JsonProperty("min")
    private Double min;

    @ApiModelProperty("最大值")
    @JsonProperty("max")
    private Double max;

    @ApiModelProperty("是否包含最小值")
    @JsonProperty("minInclusive")
    private Boolean minInclusive;

    @ApiModelProperty("是否包含最大值")
    @JsonProperty("maxInclusive")
    private Boolean maxInclusive;

    @ApiModelProperty("区间显示文本")
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