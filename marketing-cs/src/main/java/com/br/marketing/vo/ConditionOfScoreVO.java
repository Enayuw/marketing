package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public class ConditionOfScoreVO {

    @Schema(description = "id")
    private Long id;

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

    public Long getId() {
        return id;
    }

    public ConditionOfScoreVO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ConditionOfScoreVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ConditionOfScoreVO setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContentShow() {
        return contentShow;
    }

    public ConditionOfScoreVO setContentShow(String contentShow) {
        this.contentShow = contentShow;
        return this;
    }

    public String getConditionNumber() {
        return conditionNumber;
    }

    public ConditionOfScoreVO setConditionNumber(String conditionNumber) {
        this.conditionNumber = conditionNumber;
        return this;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getScoreContent() {
        return scoreContent;
    }

    public void setScoreContent(String scoreContent) {
        this.scoreContent = scoreContent;
    }

    public String getTagContent() {
        return tagContent;
    }

    public void setTagContent(String tagContent) {
        this.tagContent = tagContent;
    }
}
