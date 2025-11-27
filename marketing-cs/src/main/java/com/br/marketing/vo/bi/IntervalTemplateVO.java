package com.br.marketing.vo.bi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName IntervalTemplateVO
 * @Description
 * @Author kongbx
 * @Date 2025/8/6 19:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntervalTemplateVO {

    @Schema(description = "id")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "apiCode")
    @JsonProperty("apiCode")
    private String apiCode;

    @Schema(description = "reportId")
    @JsonProperty("reportId")
    private Long reportId;

    @Schema(description = "模板名称")
    @JsonProperty("templateName")
    private String templateName;

    @Schema(description = "模板编号")
    @JsonProperty("templateNumber")
    private String templateNumber;

    @Schema(description = "自定义区间配置列表")
    @JsonProperty("intervalModels")
    private List<IntervalModelsVO> intervalModels;

    @Data
    @Schema(description = "自定义区间模型")
    public static class IntervalModelsVO {
        @Schema(description = "id")
        @JsonProperty("id")
        private Long id;

        @Schema(description = "自定义区间配置id")
        @JsonProperty("configId")
        private Long configId;

        @Schema(description = "顺序")
        @JsonProperty("axisType")
        private String axisType;

        @Schema(description = "x轴模型")
        @JsonProperty("xModelName")
        private String xModelName;

        @Schema(description = "y轴模型")
        @JsonProperty("yModelName")
        private String yModelName;

        @Schema(description = "x轴自定义区间")
        @JsonProperty("xIntervalList")
        private String xIntervalList;

        @Schema(description = "y轴自定义区间")
        @JsonProperty("yIntervalList")
        private String yIntervalList;

        @Schema(description = "顺序")
        @JsonProperty("order")
        private String order;

    }
}
