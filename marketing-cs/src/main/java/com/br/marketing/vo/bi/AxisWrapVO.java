package com.br.marketing.vo.bi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 坐标轴数据
 *
 * @author senyang.zheng
 * @date 2024/08/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AxisWrapVO {
    @Schema(description = "X轴对应产品")
    @JsonProperty(value = "xAxisProduct")
    private String xAxisProduct;
    @Schema(description = "Y轴对应产品")
    @JsonProperty(value = "yAxisProduct")
    private String yAxisProduct;
    @Schema(description = "X轴数据")
    @JsonProperty(value = "xAxis")
    private List<String> xAxis;
    @Schema(description = "Y轴数据")
    @JsonProperty(value = "yAxis")
    private List<WrapDataVO> yAxis;
    @Schema(description = "模型分布类型 1-单模型(field_x可多个,field_y无值)；2-多模型（field_x和field_y各一个值）")
    private Integer reportScoreType;
    @Schema(description = "报表描述")
    private String statisticsDesc;
    @Schema(description = "跑分分布报表id")
    @JsonProperty(value = "statisticsId")
    private Long statisticsId;
    @Schema(description = "顺序")
    @JsonProperty(value = "order")
    private Integer order;
}
