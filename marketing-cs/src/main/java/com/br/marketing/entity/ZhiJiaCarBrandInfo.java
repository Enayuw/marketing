package com.br.marketing.entity;

import java.util.Date;

public class ZhiJiaCarBrandInfo {
    /**
     * 
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 统一后的品牌名称
     */
    private String newBrandName;

    /**
     * 品牌补充，以英文逗号分割
     */
    private String brandExtend;

    /**
     * 上传日期
     */
    private String appletDate;

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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName == null ? null : brandName.trim();
    }

    public String getNewBrandName() {
        return newBrandName;
    }

    public void setNewBrandName(String newBrandName) {
        this.newBrandName = newBrandName == null ? null : newBrandName.trim();
    }

    public String getBrandExtend() {
        return brandExtend;
    }

    public void setBrandExtend(String brandExtend) {
        this.brandExtend = brandExtend == null ? null : brandExtend.trim();
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
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