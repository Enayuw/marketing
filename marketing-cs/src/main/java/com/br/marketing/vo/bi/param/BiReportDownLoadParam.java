package com.br.marketing.vo.bi.param;

import com.br.marketing.vo.bi.WrapDataVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "BI报表下载请求参数")
public class BiReportDownLoadParam {
    @Schema(description = "报告名称")
    @NotNull(message = "报表类型不能为空")
    private String reportTypeName;
    @Schema(description = "分组")
    private String group;
    @Schema(description = "任务名称")
    private String reportTaskName;
    @Schema(description = "报告名称")
    @NotNull(message = "报表名称不能为空")
    private String reportName;
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
