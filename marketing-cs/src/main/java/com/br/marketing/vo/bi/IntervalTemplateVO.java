package com.br.marketing.vo.bi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModel;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty("自定义区间配置列表")
    @JsonProperty("intervalModels")
    private List<IntervalModelsVO> intervalModels;

    @Data
    @ApiModel("自定义区间模型")
    public static class IntervalModelsVO {
        @ApiModelProperty("id")
        @JsonProperty("id")
        private Long id;

        @ApiModelProperty("自定义区间配置id")
        @JsonProperty("configId")
        private Long configId;

        @ApiModelProperty("顺序")
        @JsonProperty("axisType")
        private String axisType;

        @ApiModelProperty("x轴模型")
        @JsonProperty("xModelName")
        private String xModelName;

        @ApiModelProperty("y轴模型")
        @JsonProperty("yModelName")
        private String yModelName;

        @ApiModelProperty("x轴自定义区间")
        @JsonProperty("xIntervalList")
        private String xIntervalList;

        @ApiModelProperty("y轴自定义区间")
        @JsonProperty("yIntervalList")
        private String yIntervalList;

        @ApiModelProperty("顺序")
        @JsonProperty("order")
        private String order;

    }
}
