package com.br.marketing.vo.bi;

import java.util.List;

import com.microsoft.schemas.office.visio.x2012.main.SheetType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @ApiModelProperty(value = "X轴对应产品")
    private String xAxisProduct;
    @ApiModelProperty(value = "Y轴对应产品")
    private String yAxisProduct;
    @ApiModelProperty(value = "X轴数据")
    private List<String> xAxis;
    @ApiModelProperty(value = "Y轴数据")
    private List<WrapDataVO> yAxis;
    @ApiModelProperty(value = "模型分布类型 1-单模型(field_x可多个,field_y无值)；2-多模型（field_x和field_y各一个值）")
    private Integer reportScoreType;
    @ApiModelProperty(value = "报表描述")
    private String statisticsDesc;
}
