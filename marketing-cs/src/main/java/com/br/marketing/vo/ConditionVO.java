package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ConditionVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "conditionId")
    private Long conditionId;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "数据源类型：0-无数据源(默认，存量的模板数据)；1-跑分数据源；2-众安转化数据源")
    private Integer sourceType;

    @Schema(description = "条件json")
    private String content;

    @Schema(description = "条件前端文本")
    private String contentShow;

    @Schema(description = "评分分布条件json")
    private String scoreContent;

    @Schema(description = "模板编号")
    private String conditionNumber;

    @Schema(description = "标签规则")
    private String tagContent;
}
