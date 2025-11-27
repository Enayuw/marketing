package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


@Data
public class FastTaskRuleDetailVO {
    /**
     * 
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    @NotEmpty(message = "规则名称不可为空！")
    private String ruleName;

    /**
     * 规则编号
     */
    @Schema(description = "规则编号")
    private String ruleNumber;

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


    @Schema(description = "跑分数据(查看页面返回字段)")
    private List<Map> dataCondition;

    /**
     * 跑分范围 1-全量；2-未跑分
     */
    @Schema(description = "跑分范围")
    @NotNull(message = "跑分范围不可为空！")
    private Integer dataType;


    @Schema(description = "未跑分数据量")
    private Integer untaskNum;

    @Schema(description = "整体跑分规则(查看页面返回字段)")
    private Map scoreRule;

    /**
     * 跑分日期
     */
    @Schema(description = "跑分日期")
    private String taskTime;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;


}