package com.br.marketing.vo.bi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "报告类型名称")
    private String reportTypeName;
    @ApiModelProperty(value = "报告名称")
    private String reportName;
    @ApiModelProperty(value = "报表类型:表格:table;折线图:line;柱状图:bar;饼图:pie")
    private String type;
    @ApiModelProperty(value = "分组")
    private String group;
    @ApiModelProperty(value = "X轴名称")
    @JsonProperty(value = "xAxisName")
    private String xAxisName;
    @ApiModelProperty(value = "X轴数据")
    @JsonProperty(value = "xAxis")
    private List<String> xAxis;
    @ApiModelProperty(value = "Y轴数据")
    @JsonProperty(value = "yAxis")
    private List<WrapDataVO> yAxis;

}
