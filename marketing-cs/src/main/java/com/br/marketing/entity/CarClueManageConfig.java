package com.br.marketing.entity;

import java.util.Date;

public class CarClueManageConfig {
    /**
     * 
     */
    private Long id;

    /**
     * 易车KA拉取时间
     */
    private String pullDate;

    /**
     * 外呼意向等级配置
     */
    private String intentionConfig;

    /**
     * 数据清洗类型 0-手动执行 1-自动执行
     */
    private Integer cleanType;

    /**
     * 数据推送类型 0-手动执行 1-自动执行
     */
    private Integer pullType;

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

    public String getPullDate() {
        return pullDate;
    }

    public void setPullDate(String pullDate) {
        this.pullDate = pullDate == null ? null : pullDate.trim();
    }

    public String getIntentionConfig() {
        return intentionConfig;
    }

    public void setIntentionConfig(String intentionConfig) {
        this.intentionConfig = intentionConfig == null ? null : intentionConfig.trim();
    }

    public Integer getCleanType() {
        return cleanType;
    }

    public void setCleanType(Integer cleanType) {
        this.cleanType = cleanType;
    }

    public Integer getPullType() {
        return pullType;
    }

    public void setPullType(Integer pullType) {
        this.pullType = pullType;
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