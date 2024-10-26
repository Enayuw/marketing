package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ScoreConditionDetailVO {

    @ApiModelProperty(value = "规则id")
    private Long id;

    @ApiModelProperty(value = "规则编号")
    private String conditionNumber;

    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @ApiModelProperty(value = "状态 1-开始；2-关闭")
    private Integer status;

    @ApiModelProperty(value = "规则名称")
    private String name;

    @ApiModelProperty(value = "规则内容")
    private String contentShow;

    @ApiModelProperty(value = "评分分布规则内容")
    private String scoreContentShow;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;
}
