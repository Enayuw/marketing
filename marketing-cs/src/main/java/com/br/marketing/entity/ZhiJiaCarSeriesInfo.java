package com.br.marketing.entity;

import java.util.Date;

public class ZhiJiaCarSeriesInfo {
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
     * 车系id
     */
    private Integer seriesId;

    /**
     * 车系名称
     */
    private String seriesName;

    /**
     * 统一后的车系名称
     */
    private String newSeriesName;

    /**
     * 车系补充，以英文逗号分割
     */
    private String seriesExtend;

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

    public Integer getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName == null ? null : seriesName.trim();
    }

    public String getNewSeriesName() {
        return newSeriesName;
    }

    public void setNewSeriesName(String newSeriesName) {
        this.newSeriesName = newSeriesName == null ? null : newSeriesName.trim();
    }

    public String getSeriesExtend() {
        return seriesExtend;
    }

    public void setSeriesExtend(String seriesExtend) {
        this.seriesExtend = seriesExtend == null ? null : seriesExtend.trim();
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