package com.br.marketing.vo.bi;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Y轴数据
 *
 * @author senyang.zheng
 * @date 2024/08/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WrapDataVo {
    @ApiModelProperty(value = "Y轴名称")
    private String name;
    @ApiModelProperty(value = "Y轴数据")
    private List<String> data;
}
