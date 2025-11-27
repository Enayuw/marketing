package com.br.marketing.vo.bi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * BI报表 VO
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiReportVO {

    @Schema(description = "报告类型名称")
    private String reportTypeName;
    @Schema(description = "报告名称")
    private String reportName;
    @Schema(description = "报表类型:表格:table;折线图:line;柱状图:bar;饼图:pie")
    private String type;
    @Schema(description = "分组")
    private String group;
    @Schema(description = "X轴名称")
    @JsonProperty(value = "xAxisName")
    private String xAxisName;
    @Schema(description = "X轴数据")
    @JsonProperty(value = "xAxis")
    private List<String> xAxis;
    @Schema(description = "Y轴数据")
    @JsonProperty(value = "yAxis")
    private List<WrapDataVO> yAxis;

}
