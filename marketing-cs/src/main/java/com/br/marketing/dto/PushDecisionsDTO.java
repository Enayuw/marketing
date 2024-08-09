package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;

/**
 * @ClassName PushDecisionsDTO
 * @Description 推送决策配置
 * @Author kongbx
 * @Date 2024/8/9 10:25
 */
public class PushDecisionsDTO {

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @ApiModelProperty(value = "规则编号")
    @NotNull(message = "规则编号不能为空")
    private String ruleNumber;

    @ApiModelProperty(value = "规则名称")
    @NotNull(message = "规则名称不能为空")
    private String ruleName;

    @ApiModelProperty(value = "依赖模板id")
    @NotNull(message = "依赖模板id不能为空")
    private Integer dependencyTemplateId;

    @ApiModelProperty(value = "规则状态 1-启用;2-禁用")
    @NotNull(message = "规则状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "每日自动执行时间")
    @NotNull(message = "每日自动执行时间不能为空")
    private String autoTime;

    @ApiModelProperty(value = "推送数据集")
    @NotNull(message = "推送数据集不能为空")
    private String pushDatasets;

    @ApiModelProperty(value = "触达策略")
    @NotNull(message = "触达策略不能为空")
    private String reachStrategy;

}
