package com.br.marketing.entity;

import java.util.Date;

public class BizTrackingTemplateEdge {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 起始节点ID
     */
    private Long fromNodeId;

    /**
     * 目标节点ID
     */
    private Long toNodeId;

    /**
     * 边类型：SOLID-实线(必须) DASHED-虚线(可选)
     */
    private String edgeType;

    /**
     * 边描述
     */
    private String description;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId == null ? null : templateId.trim();
    }

    public Long getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(Long fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public Long getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(Long toNodeId) {
        this.toNodeId = toNodeId;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType == null ? null : edgeType.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
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
}