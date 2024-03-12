package com.br.marketing.entity;

import java.util.Date;

public class MarketingDataValidConfig {
    /**
     * 
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 上传日期
     */
    private String appletDate;

    /**
     * 场景
     */
    private String userType;

    /**
     * 生效开始日期
     */
    private String validStartDate;

    /**
     * 生效结束日期
     */
    private String validEndDate;

    /**
     * T+N 有效期方式配置
     */
    private String validDays;

    /**
     * 1-有效期范围配置；2-有效期T+N配置
     */
    private Integer validType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getValidStartDate() {
        return validStartDate;
    }

    public void setValidStartDate(String validStartDate) {
        this.validStartDate = validStartDate == null ? null : validStartDate.trim();
    }

    public String getValidEndDate() {
        return validEndDate;
    }

    public void setValidEndDate(String validEndDate) {
        this.validEndDate = validEndDate == null ? null : validEndDate.trim();
    }

    public String getValidDays() {
        return validDays;
    }

    public void setValidDays(String validDays) {
        this.validDays = validDays == null ? null : validDays.trim();
    }

    public Integer getValidType() {
        return validType;
    }

    public void setValidType(Integer validType) {
        this.validType = validType;
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