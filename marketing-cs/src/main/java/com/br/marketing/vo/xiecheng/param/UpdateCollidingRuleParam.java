package com.br.marketing.vo.xiecheng.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "修改撞库规则参数")
public class UpdateCollidingRuleParam {

    @ApiModelProperty("主键id")
    private Long dprId;

    @ApiModelProperty("数据包名称")
    private String packageName;

    @ApiModelProperty("原-设定撞得量级")
    private Integer originalCollidingBackNumber;

    @ApiModelProperty("原-一天内的撞库次数")
    private Integer originalCollidingTimes;

    @ApiModelProperty("原-开启撞库时间 yyyy-MM-dd HH:mm:ss")
    private String originalCollidingStartTime;

    @ApiModelProperty("原-结束撞库时间 yyyy-MM-dd HH:mm:ss")
    private String originalCollidingEndTime;

    @ApiModelProperty("设定撞得量级")
    private Integer collidingBackNumber;

    @ApiModelProperty("一天内的撞库次数")
    private Integer collidingTimes;

    @ApiModelProperty("开启撞库时间 yyyy-MM-dd HH:mm:ss")
    private String collidingStartTime;

    @ApiModelProperty("结束撞库时间 yyyy-MM-dd HH:mm:ss")
    private String collidingEndTime;

}
