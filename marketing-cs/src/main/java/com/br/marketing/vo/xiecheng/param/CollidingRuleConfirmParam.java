package com.br.marketing.vo.xiecheng.param;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "携程撞库规则 确认/暂存 参数")
public class CollidingRuleConfirmParam implements Serializable {

    private static final long serialVersionUID = 1270257474084816056L;

    @ApiModelProperty("主键id")
    private Long prsId;

    @ApiModelProperty("商户编号")
    private String apiCode;

    @ApiModelProperty("携程撞库包的id")
    private Long packageId;

    @ApiModelProperty("撞库数据清洗任务id")
    private Long collidingDataTaskId;

    @ApiModelProperty("撞得量级")
    private Integer collidingBackNumber;

    @ApiModelProperty("撞库开始时间")
    private String collidingStartTime;

    @ApiModelProperty("撞库结束时间")
    private String collidingEndTime;

    @ApiModelProperty("一天内的撞库次数")
    private Integer collidingTimes;
}
