package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ConditionVO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "conditionId")
    private Long conditionId;

    @ApiModelProperty(value = "模板名称")
    private String name;

    @ApiModelProperty(value = "数据源类型：0-无数据源(默认，存量的模板数据)；1-跑分数据源；2-众安转化数据源")
    private Integer sourceType;

    @ApiModelProperty(value = "条件json")
    private String content;

    @ApiModelProperty(value = "条件前端文本")
    private String contentShow;

    @ApiModelProperty(value = "评分分布条件json")
    private String scoreContent;

    @ApiModelProperty(value = "模板编号")
    private String conditionNumber;

    @ApiModelProperty(value = "标签规则")
    private String tagContent;
}
