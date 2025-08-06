package com.br.marketing.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 刷新报表请求DTO
 *
 * @author bingxu.kong
 * @date 2025-01-07
 */
@Data
@ApiModel("刷新报表请求DTO")
public class RefreshReportRequestDTO {

    @ApiModelProperty("报表任务ID")
    @JsonProperty("reportId")
    private Long reportId;

    @ApiModelProperty("自定义区间配置列表")
    @JsonProperty("customIntervals")
    private List<CustomIntervalConfigDTO> customIntervals;

    @Data
    @ApiModel("自定义区间配置")
    public static class CustomIntervalConfigDTO {
        
        @ApiModelProperty("统计配置ID")
        @JsonProperty("statisticsId")
        private Long statisticsId;

        @ApiModelProperty("模型类型：1-单模型，2-多模型")
        @JsonProperty("reportScoreType")
        private Integer reportScoreType;

        @ApiModelProperty("X模型名称")
        @JsonProperty("fieldX")
        private String fieldX;

        @ApiModelProperty("Y模型名称")
        @JsonProperty("fieldY")
        private String fieldY;

        @ApiModelProperty("X区间配置列表")
        @JsonProperty("xIntervalList")
        private List<IntervalRangeDTO> xIntervalList;

        @ApiModelProperty("Y区间配置列表")
        @JsonProperty("yIntervalList")
        private List<IntervalRangeDTO> yIntervalList;
    }
}