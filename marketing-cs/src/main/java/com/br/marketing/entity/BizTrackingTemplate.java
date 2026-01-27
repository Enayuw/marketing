package com.br.marketing.entity;

import java.util.Date;

public class BizTrackingTemplate {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 状态：0-禁用 1-启用
     */
    private Byte status;

    /**
     * 链路图结构（JSON格式，包含节点、连线、位置等完整信息）
     */
    private String graphJson;

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

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName == null ? null : templateName.trim();
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

    public String getGraphJson() {
        return graphJson;
    }

    public void setGraphJson(String graphJson) {
        this.graphJson = graphJson == null ? null : graphJson.trim();
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