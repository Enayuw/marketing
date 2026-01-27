package com.br.marketing.entity;

import java.util.Date;

public class BizTrackingTemplateNode {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 节点字典ID
     */
    private Long nodeDictId;

    /**
     * 节点名称
     */
    private String nodeName;

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

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getNodeDictId() {
        return nodeDictId;
    }

    public void setNodeDictId(Long nodeDictId) {
        this.nodeDictId = nodeDictId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName == null ? null : nodeName.trim();
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