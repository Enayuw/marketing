package com.br.marketing.vo.xiecheng;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "携程撞库暂存规则VO")
public class XiechengCollidingStagingRuleVO implements Serializable {
    private static final long serialVersionUID = -6620381451286081664L;

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("ApiCode")
    private String apiCode;

    @ApiModelProperty("携程撞库包的id")
    private Long packageId;

    @ApiModelProperty("撞库数据清洗任务id")
    private Long collidingDataTaskId;

    @ApiModelProperty("撞得量级")
    private Integer collidingBackNumber;

    @ApiModelProperty("撞库开始时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date collidingStartTime;

    @ApiModelProperty("撞库结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date collidingEndTime;

    @ApiModelProperty("一天内的撞库次数")
    private Integer collidingTimes;

    @ApiModelProperty("创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @ApiModelProperty("是否删除 0 正常，1删除")
    private Integer isDelete;
}
