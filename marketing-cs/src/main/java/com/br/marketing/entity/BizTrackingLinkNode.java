package com.br.marketing.entity;

import java.util.Date;

public class BizTrackingLinkNode {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 链路ID
     */
    private Long linkId;

    /**
     * 节点ID
     */
    private Long nodeId;

    /**
     * 节点字典ID
     */
    private Long nodeDictId;

    /**
     * 节点别名（在链路中的显示名称）
     */
    private String nodeAlias;

    /**
     * 边类型：SOLID/DASHED
     */
    private String edgeType;

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

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getNodeDictId() {
        return nodeDictId;
    }

    public void setNodeDictId(Long nodeDictId) {
        this.nodeDictId = nodeDictId;
    }

    public String getNodeAlias() {
        return nodeAlias;
    }

    public void setNodeAlias(String nodeAlias) {
        this.nodeAlias = nodeAlias == null ? null : nodeAlias.trim();
    }

    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType == null ? null : edgeType.trim();
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
}