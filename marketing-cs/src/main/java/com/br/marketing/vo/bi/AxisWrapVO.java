package com.br.marketing.vo.bi;

import java.util.List;

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
}
