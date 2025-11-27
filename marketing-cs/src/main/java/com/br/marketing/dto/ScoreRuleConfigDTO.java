package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description
 * @Date 2022/5/9 6:11 PM
 * ------------------------------
 */
@Data
public class ScoreRuleConfigDTO {


    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    @NotEmpty(message = "规则名称不可为空！")
    private String ruleName;


    @Schema(description = "apiCode")
    private String apiCode;

    /**
     * 规则id
     */
    @Schema(description = "整体跑分规则id,选多个逗号分隔")
    @NotEmpty(message = "跑分规则不可为空！")
    private String ruleIds;
    /**
     * 跑分类型 0 正常跑分 1 不跑分 2 产品配置
     */
    @Schema(description = "跑分类型")
    @NotNull(message = "跑分类型不可为空！")
    private Integer taskType;


    @Schema(description = "跑分数据所选的数据id，逗号分隔")
    @NotEmpty(message = "跑分数据不可为空！")
    private String dataIdDesc;


    /**
     * 跑分范围 1-全量；2-未跑分
     */
    @Schema(description = "跑分范围")
    @NotNull(message = "跑分范围不可为空！")
    private Integer dataType;


    /**
     * 跑分日期
     */
    @Schema(description = "跑分日期")
    private String taskTime;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description="跑分数据范围")
    private String conditionInfo;
}
