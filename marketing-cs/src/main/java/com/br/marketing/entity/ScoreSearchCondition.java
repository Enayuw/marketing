package com.br.marketing.entity;

import java.util.Date;

public class ScoreSearchCondition {
    /**
     * 
     */
    private Long id;

    /**
     * 模板编号
     */
    private String conditionNumber;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 1-启用;2-禁用
     */
    private Integer status;

    /**
     * 条件用途 1-规则模板；2-转化筛选
     */
    private Integer conditionType;

    /**
     * 数据源类型：0-无数据源(默认，存量的模板数据)；1-跑分数据源；2-众安转化数据源
     */
    private Integer sourceType;

    /**
     * 数据源字段
     */
    private String sourceCondition;

    /**
     * 条件内容
     */
    private String content;

    /**
     * 前端解析文本
     */
    private String contentShow;

    /**
     * 评分分布条件内容
     */
    private String scoreContent;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 标签条件内容
     */
    private String tagContent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConditionNumber() {
        return conditionNumber;
    }

    public void setConditionNumber(String conditionNumber) {
        this.conditionNumber = conditionNumber == null ? null : conditionNumber.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getConditionType() {
        return conditionType;
    }

    public void setConditionType(Integer conditionType) {
        this.conditionType = conditionType;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceCondition() {
        return sourceCondition;
    }

    public void setSourceCondition(String sourceCondition) {
        this.sourceCondition = sourceCondition == null ? null : sourceCondition.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getContentShow() {
        return contentShow;
    }

    public void setContentShow(String contentShow) {
        this.contentShow = contentShow == null ? null : contentShow.trim();
    }

    public String getScoreContent() {
        return scoreContent;
    }

    public void setScoreContent(String scoreContent) {
        this.scoreContent = scoreContent == null ? null : scoreContent.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getTagContent() {
        return tagContent;
    }

    public void setTagContent(String tagContent) {
        this.tagContent = tagContent == null ? null : tagContent.trim();
    }
}