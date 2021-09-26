package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class SoleRuleSearchDTO {

    @ApiModelProperty(value = "去重规则名称")
    private String soleName;

    @ApiModelProperty(value = "开启状态;(1-开启;2-禁用;不传查全部)")
    private Integer status;

    @ApiModelProperty(value = "开始创建时间")
    private String createTimeStart;

    @ApiModelProperty(value = "结束创建时间")
    private String createTimeEnd;

    @ApiModelProperty(value = "开始变更时间")
    private String updateTimeStart;

    @ApiModelProperty(value = "结束变更时间")
    private String updateTimeEnd;

}
