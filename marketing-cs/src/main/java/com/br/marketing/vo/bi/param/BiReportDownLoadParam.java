package com.br.marketing.vo.bi.param;

import com.br.marketing.vo.bi.WrapDataVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * BI报表下载请求参数
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Getter
@Setter
@ApiModel(value = "BI报表下载请求参数")
public class BiReportDownLoadParam {
    @ApiModelProperty(value = "报告名称")
    @NotNull(message = "报表类型不能为空")
    private String reportTypeName;
    @ApiModelProperty(value = "报告名称")
    @NotNull(message = "报表名称不能为空")
    private String reportName;
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
