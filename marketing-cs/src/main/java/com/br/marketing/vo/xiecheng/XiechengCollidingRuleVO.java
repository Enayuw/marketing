package com.br.marketing.vo.xiecheng;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "携程撞库规则VO")
public class XiechengCollidingRuleVO implements Serializable {
    private static final long serialVersionUID = 6038761827928494339L;

    @ApiModelProperty("规则主键id")
    private Long dprId;

    @ApiModelProperty("客户编号")
    private String cid;

    @ApiModelProperty("ApiCode")
    private String apiCode;

    @ApiModelProperty("客户名称")
    private String shortName;

    @ApiModelProperty("包主键id")
    private Long pkgId;

    @ApiModelProperty("数据包名称")
    private String packageName;

    @ApiModelProperty("预估量级")
    private String discreetNumber;

    @ApiModelProperty("实际可用量级")
    private String remainingNumber;

    @ApiModelProperty("任务状态")
    private Integer collidingSwitch;

    @ApiModelProperty("优先级")
    private Integer priority;

    @ApiModelProperty("设定撞得量级")
    private Integer collidingBackNumber;

    @ApiModelProperty("每日撞库次数")
    private Integer collidingTimes;

    @ApiModelProperty("数据清洗时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskStartTime;

    @ApiModelProperty("开启撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date collidingStartTime;

    @ApiModelProperty("结束撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date collidingEndTime;

    @ApiModelProperty("创建时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("修改时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
