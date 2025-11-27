package com.br.marketing.vo.bi.param;

import com.br.marketing.vo.bi.WrapDataVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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

}
