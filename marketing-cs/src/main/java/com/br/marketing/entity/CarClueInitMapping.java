package com.br.marketing.entity;

import java.util.Date;

public class CarClueInitMapping {
    /**
     * 
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 车系名称
     */
    private String seriesName;

    /**
     * 全国
     */
    private String nation;

    /**
     * 固定省份名称
     */
    private String satisfyProvinceName;

    /**
     * 固定城市名称
     */
    private String satisfyCityName;

    /**
     * 排除省份名称
     */
    private String excludeProvinceName;

    /**
     * 排除城市名称
     */
    private String excludeCityName;

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

    /**
     * 日限量
     */
    private Integer dailyLimited;

    /**
     * 需求ID
     */
    private String demandId;

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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName == null ? null : brandName.trim();
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName == null ? null : seriesName.trim();
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation == null ? null : nation.trim();
    }

    public String getSatisfyProvinceName() {
        return satisfyProvinceName;
    }

    public void setSatisfyProvinceName(String satisfyProvinceName) {
        this.satisfyProvinceName = satisfyProvinceName == null ? null : satisfyProvinceName.trim();
    }

    public String getSatisfyCityName() {
        return satisfyCityName;
    }

    public void setSatisfyCityName(String satisfyCityName) {
        this.satisfyCityName = satisfyCityName == null ? null : satisfyCityName.trim();
    }

    public String getExcludeProvinceName() {
        return excludeProvinceName;
    }

    public void setExcludeProvinceName(String excludeProvinceName) {
        this.excludeProvinceName = excludeProvinceName == null ? null : excludeProvinceName.trim();
    }

    public String getExcludeCityName() {
        return excludeCityName;
    }

    public void setExcludeCityName(String excludeCityName) {
        this.excludeCityName = excludeCityName == null ? null : excludeCityName.trim();
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

    public Integer getDailyLimited() {
        return dailyLimited;
    }

    public void setDailyLimited(Integer dailyLimited) {
        this.dailyLimited = dailyLimited;
    }

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId == null ? null : demandId.trim();
    }
}