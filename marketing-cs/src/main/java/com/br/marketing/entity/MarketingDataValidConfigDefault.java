package com.br.marketing.entity;

import java.util.Date;

public class MarketingDataValidConfigDefault {
    /**
     * id
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 上传日期
     */
    private Date uploadDate;

    /**
     * 场景
     */
    private String userType;

    /**
     * 默认有效期是N天，代表T+N范围
     */
    private Integer validDaysDefault;

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

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public Integer getValidDaysDefault() {
        return validDaysDefault;
    }

    public void setValidDaysDefault(Integer validDaysDefault) {
        this.validDaysDefault = validDaysDefault;
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