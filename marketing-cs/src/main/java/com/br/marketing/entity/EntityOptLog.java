package com.br.marketing.entity;

import java.util.Date;

public class EntityOptLog {
    /**
     * 
     */
    private Long id;

    /**
     * 修改的记录id
     */
    private String sourceId;

    /**
     * 修改的表名
     */
    private String sourceObj;

    /**
     * 修改对应的实体
     */
    private String sourceEntity;

    /**
     * 修改内容 格式【字段名】=【oldvalue】->【newvalue】
     */
    private String content;

    /**
     * 描述
     */
    private String desc;

    /**
     * 操作人id
     */
    private String optUserId;

    /**
     * 操作人姓名
     */
    private String optUserName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 操作类型1-新增;2-修改;
     */
    private Integer optType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId == null ? null : sourceId.trim();
    }

    public String getSourceObj() {
        return sourceObj;
    }

    public void setSourceObj(String sourceObj) {
        this.sourceObj = sourceObj == null ? null : sourceObj.trim();
    }

    public String getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(String sourceEntity) {
        this.sourceEntity = sourceEntity == null ? null : sourceEntity.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }

    public String getOptUserId() {
        return optUserId;
    }

    public void setOptUserId(String optUserId) {
        this.optUserId = optUserId == null ? null : optUserId.trim();
    }

    public String getOptUserName() {
        return optUserName;
    }

    public void setOptUserName(String optUserName) {
        this.optUserName = optUserName == null ? null : optUserName.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getOptType() {
        return optType;
    }

    public void setOptType(Integer optType) {
        this.optType = optType;
    }
}