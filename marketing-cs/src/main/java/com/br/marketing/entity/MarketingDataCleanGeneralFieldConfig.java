package com.br.marketing.entity;

import java.util.Date;

public class MarketingDataCleanGeneralFieldConfig {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 数据来源,0-营销中台 1-外呼系统
     */
    private Integer systemType;

    /**
     * 数据类型：0:上传，1:转化
     */
    private Integer dataType;

    /**
     * 字段集合，多个字段用,分割
     */
    private String fieldCollect;

    /**
     * 是否删除：1-正常；9-删除
     */
    private Integer isDel;

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
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSystemType() {
        return systemType;
    }

    public void setSystemType(Integer systemType) {
        this.systemType = systemType;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getFieldCollect() {
        return fieldCollect;
    }

    public void setFieldCollect(String fieldCollect) {
        this.fieldCollect = fieldCollect == null ? null : fieldCollect.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
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
}