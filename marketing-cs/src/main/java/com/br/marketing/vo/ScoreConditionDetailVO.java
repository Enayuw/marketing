package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScoreConditionDetailVO {

    @Schema(description = "规则id")
    private Long id;

    @Schema(description = "规则编号")
    private String conditionNumber;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "状态 1-开始；2-关闭")
    private Integer status;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则内容")
    private String contentShow;

    @Schema(description = "标签规则")
    private String tagContent;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
