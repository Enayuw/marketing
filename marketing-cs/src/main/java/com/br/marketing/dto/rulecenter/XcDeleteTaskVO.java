package com.br.marketing.dto.rulecenter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@ApiModel(value = "携程剔除任务VO")
public class XcDeleteTaskVO {

    @ApiModelProperty("规则主键id")
    private Long dprId;

    @ApiModelProperty("客户编号")
    private String cid;

    @ApiModelProperty("ApiCode")
    private String apiCode;

    @ApiModelProperty("客户名称")
    private String shortName;

    @ApiModelProperty("预估剔除量级")
    private String discreetNumber;

    @ApiModelProperty("实际剔除量级")
    private String actualNumber;

    @ApiModelProperty(value = "releaseTime开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeBegin;

    @ApiModelProperty(value = "releaseTime结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeEnd;

    @ApiModelProperty(value = "剔除类型")
    private Integer taskType;

    @ApiModelProperty(value = "剔除任务执行时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime taskExecuteTime;

    @ApiModelProperty("任务执行状态")
    private Integer taskStatus;

    @ApiModelProperty("创建时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
}
