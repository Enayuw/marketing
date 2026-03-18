package com.br.marketing.entity;

import java.util.Date;

public class BizTrackingLinkEdge {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 链路ID
     */
    private Long linkId;

    /**
     * 起始节点ID（关联biz_tracking_link_node.id）
     */
    private Long fromNodeId;

    /**
     * 目标节点ID（关联biz_tracking_link_node.id）
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
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
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
}