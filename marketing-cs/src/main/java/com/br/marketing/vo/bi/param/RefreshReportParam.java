package com.br.marketing.vo.bi.param;

import com.br.marketing.vo.bi.WrapDataVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName RefreshReportParam
 * @Description 重刷报表对象
 * @Author kongbx
 * @Date 2025/8/5 14:31
 */
@Data
public class RefreshReportParam extends ReportTaskParam{

    @ApiModelProperty(value = "X轴对应产品")
    @JsonProperty(value = "xAxisProduct")
    private String xAxisProduct;
    @ApiModelProperty(value = "Y轴对应产品")
    @JsonProperty(value = "yAxisProduct")
    private String yAxisProduct;
    @ApiModelProperty(value = "X轴数据")
    @JsonProperty(value = "xAxis")
    private List<String> xAxis;
    @ApiModelProperty(value = "Y轴数据")
    @JsonProperty(value = "yAxis")
    private List<WrapDataVO> yAxis;
    @ApiModelProperty(value = "模型分布类型 1-单模型(field_x可多个,field_y无值)；2-多模型（field_x和field_y各一个值）")
    private Integer reportScoreType;

}
