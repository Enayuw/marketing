package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName PushDecisionsDetailVO
 * @Description TODO
 * @Author kongbx
 * @Date 2024/8/9 10:54
 */
@Data
public class PushDecisionsDetailVO {

    @Schema(description = "规则id")
    private Long id;

    @Schema(description = "商户编号")
    private String apiCode;

    @Schema(description = "规则编号")
    private String ruleNumber;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "依赖模板id")
    private Long dependencyTemplateId;

    @Schema(description = "依赖模板名称")
    private String dependencyTemplateName;

    @Schema(description = "数据源")
    private Integer dependencyTemplateSource;

    @Schema(description = "规则状态")
    private Integer status;

    @Schema(description = "每日自动执行时间")
    private String autoTime;

    @Schema(description = "推送数据集")
    private String pushDatasets;

    @Schema(description = "触达策略")
    private String reachStrategy;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "是否自动刷新 0-否，1-是")
    private Integer autoRefresh;

    @Schema(description = "推送系统类型")
    private Integer pushTarget;
}
