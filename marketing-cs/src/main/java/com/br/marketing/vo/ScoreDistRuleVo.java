package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;

public class ScoreDistRuleVo {

    @ApiModelProperty(value = "规则id")
    private Long id;

    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @ApiModelProperty(value = "规则模板编号")
    private String templateNumber;

    @ApiModelProperty(value = "规则名称")
    private String templateName;

    @ApiModelProperty(value = "状态 1-启用；2-禁用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;
}
