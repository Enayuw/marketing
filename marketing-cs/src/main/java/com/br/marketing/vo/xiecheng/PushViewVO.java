package com.br.marketing.vo.xiecheng;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "规则中心-预览数据接口")
public class PushViewVO {

    @ApiModelProperty("筛选结果")
    private String result;

    @ApiModelProperty("筛选数量")
    private Integer total;

}
