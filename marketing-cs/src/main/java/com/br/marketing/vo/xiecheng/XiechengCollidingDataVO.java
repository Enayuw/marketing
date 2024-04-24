package com.br.marketing.vo.xiecheng;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "规则中心携程撞库数据")
public class XiechengCollidingDataVO {


    @ApiModelProperty("ApiCode")
    private String apiCode;

    @ApiModelProperty("撞库结果数据")
    private String resultData;


    @ApiModelProperty("更新时间")
    private String updateTime;

    @ApiModelProperty("数据包数据量")
    private String resultNum;

}
