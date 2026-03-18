package com.br.marketing.entity;

import java.util.Date;

public class BizTrackingLink {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 链路标识
     */
    private String linkCode;

    /**
     * 链路名称
     */
    private String linkName;

    /**
     * 业务场景 催收/贷后等
     */
    private String bizScene;

    /**
     * 链路描述
     */
    private String description;

    /**
     * 状态（0-禁用 1-启用）
     */
    private Byte status;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 链路图结构（JSON格式，包含节点、连线、位置等完整信息）
     */
    private String graphJson;

    /**
     * 客户编号/API编码
     */
    private String apiCode;

    /**
     * 匹配的模板ID
     */
    private String templateId;

    /**
     * 来源：MANUAL/AUTO
     */
    private String sourceType;

    /**
     * 自动匹配时间
     */
    private Date matchTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLinkCode() {
        return linkCode;
    }

    public void setLinkCode(String linkCode) {
        this.linkCode = linkCode == null ? null : linkCode.trim();
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName == null ? null : linkName.trim();
    }

    public String getBizScene() {
        return bizScene;
    }

    public void setBizScene(String bizScene) {
        this.bizScene = bizScene == null ? null : bizScene.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getGraphJson() {
        return graphJson;
    }

    public void setGraphJson(String graphJson) {
        this.graphJson = graphJson == null ? null : graphJson.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId == null ? null : templateId.trim();
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType == null ? null : sourceType.trim();
    }

    public Date getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(Date matchTime) {
        this.matchTime = matchTime;
    }
}