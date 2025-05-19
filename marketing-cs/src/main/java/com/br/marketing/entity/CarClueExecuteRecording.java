package com.br.marketing.entity;

import java.util.Date;

public class CarClueExecuteRecording {
    /**
     * 
     */
    private Long id;

    /**
     * 线索勾选数据
     */
    private String clueIds;

    /**
     * 线索查询条件
     */
    private String clueRange;

    /**
     * 执行类型 0-清洗 1-推送
     */
    private Integer executeType;

    /**
     * 执行状态 0-待执行；1-执行完成；
     */
    private Integer executeStatus;

    /**
     * 操作人id
     */
    private Long optUserId;

    /**
     * 操作人账户名
     */
    private String optUserName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClueIds() {
        return clueIds;
    }

    public void setClueIds(String clueIds) {
        this.clueIds = clueIds == null ? null : clueIds.trim();
    }

    public String getClueRange() {
        return clueRange;
    }

    public void setClueRange(String clueRange) {
        this.clueRange = clueRange == null ? null : clueRange.trim();
    }

    public Integer getExecuteType() {
        return executeType;
    }

    public void setExecuteType(Integer executeType) {
        this.executeType = executeType;
    }

    public Integer getExecuteStatus() {
        return executeStatus;
    }

    public void setExecuteStatus(Integer executeStatus) {
        this.executeStatus = executeStatus;
    }

    public Long getOptUserId() {
        return optUserId;
    }

    public void setOptUserId(Long optUserId) {
        this.optUserId = optUserId;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }
}