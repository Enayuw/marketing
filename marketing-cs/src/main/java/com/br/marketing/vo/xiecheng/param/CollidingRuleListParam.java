package com.br.marketing.vo.xiecheng.param;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "携程撞库规则列表查询参数")
public class CollidingRuleListParam implements Serializable {

    private static final long serialVersionUID = -5816759852739248423L;
    @ApiModelProperty("数据包名称")
    private String keyword;

    @ApiModelProperty("ApiCode")
    private String apiCode;

    @ApiModelProperty("任务状态")
    private Integer collidingSwitch;

    @ApiModelProperty("开启撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private String collidingStartTime;

    @ApiModelProperty("结束撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private String collidingEndTime;

    @ApiModelProperty(value = "当前页数")
    private Integer page = 1;

    @ApiModelProperty(value = "每页显示条数")
    private Integer pageSize = 20;

}
