package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * @ClassName PushDecisionsDTO
 * @Description 推送决策配置
 * @Author kongbx
 * @Date 2024/8/9 10:25
 */
@Data
public class PushDecisionsDTO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @Schema(description = "规则名称")
    @NotNull(message = "规则名称不能为空")
    private String ruleName;

    @Schema(description = "依赖模板id")
    @NotNull(message = "依赖模板id不能为空")
    private Long dependencyTemplateId;

    @Schema(description = "规则状态 1-启用;2-禁用")
    private Integer status;

    @Schema(description = "每日自动执行时间")
    private String autoTime;

    @Schema(description = "推送数据集")
    private String pushDatasets;

    @Schema(description = "触达策略")
    private String reachStrategy;

    @Schema(description = "跑分任务ids")
    private String fileIds;

    @Schema(description = "是否自动刷新 0-否，1-是")
    private Integer autoRefresh;

    @Schema(description = "推送系统类型")
    private Integer pushTarget;

}
