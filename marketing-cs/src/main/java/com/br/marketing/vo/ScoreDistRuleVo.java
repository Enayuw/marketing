package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScoreDistRuleVo {

    @Schema(description = "规则id")
    private Long id;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "规则模板编号")
    private String templateNumber;

    @Schema(description = "规则名称")
    private String templateName;

    @Schema(description = "状态 1-启用；2-禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
