package com.br.marketing.dto.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 刷新报表请求DTO
 *
 * @author bingxu.kong
 * @date 2025-01-07
 */
@Data
@Schema(description = "刷新报表请求DTO")
public class RefreshReportRequestDTO {

    @Schema(description = "报表任务ID")
    @JsonProperty("reportId")
    private Long reportId;

    @Schema(description = "apiCode")
    @JsonProperty("apiCode")
    private String apiCode;

    @Schema(description = "规则模板名称")
    @JsonProperty("templateName")
    private String templateName;

    @Schema(description = "自定义区间配置列表")
    @JsonProperty("customIntervals")
    private List<CustomIntervalConfigDTO> customIntervals;

    @Data
    @Schema(description = "自定义区间配置")
    public static class CustomIntervalConfigDTO {
        
        @Schema(description = "统计配置ID")
        @JsonProperty("statisticsId")
        private Long statisticsId;

        @Schema(description = "模型类型：1-单模型，2-多模型")
        @JsonProperty("reportScoreType")
        private Integer reportScoreType;

        @Schema(description = "X模型名称")
        @JsonProperty("fieldX")
        private String fieldX;

        @Schema(description = "Y模型名称")
        @JsonProperty("fieldY")
        private String fieldY;

        @Schema(description = "顺序")
        @JsonProperty("order")
        private Integer order;

        @Schema(description = "X区间配置列表")
        @JsonProperty("xIntervalList")
        private List<IntervalRangeDTO> xIntervalList;

        @Schema(description = "Y区间配置列表")
        @JsonProperty("yIntervalList")
        private List<IntervalRangeDTO> yIntervalList;
    }
}