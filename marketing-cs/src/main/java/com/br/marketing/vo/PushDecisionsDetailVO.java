package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName PushDecisionsDetailVO
 * @Description TODO
 * @Author kongbx
 * @Date 2024/8/9 10:54
 */
@Data
public class PushDecisionsDetailVO {

    @ApiModelProperty(value = "规则id")
    private Long id;

    @ApiModelProperty(value = "商户编号")
    private String apiCode;

    @ApiModelProperty(value = "规则编号")
    private String ruleNumber;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    @ApiModelProperty(value = "依赖模板")
    private String dependencyTemplate;

    @ApiModelProperty(value = "规则状态")
    private String status;

    @ApiModelProperty(value = "每日自动执行时间")
    private String autoTime;

    @ApiModelProperty(value = "推送数据集")
    private String pushDatasets;

    @ApiModelProperty(value = "触达策略")
    private String reachStrategy;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "修改时间")
    private String updateTime;
}
