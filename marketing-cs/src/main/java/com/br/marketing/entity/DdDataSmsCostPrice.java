package com.br.marketing.entity;

public class DdDataSmsCostPrice {
    /**
     * 
     */
    private Long id;

    /**
     * 供应商名称
     */
    private String lineSupplier;

    /**
     * 短信线路名称
     */
    private String lineName;

    /**
     * 有效期日期
     */
    private String effectDate;

    /**
     * 是否计算成本
     */
    private String isCalcCost;

    /**
     * 单价
     */
    private String price;

    /**
     * 创建人unionId
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private String createdTime;

    /**
     * 最近修改人unionId
     */
    private String lastModifiedBy;

    /**
     * 最近修改时间
     */
    private String lastModifiedTime;

    /**
     * 最近修改人userId
     */
    private String lastModifiedUserId;

    /**
     * 最近修改人name
     */
    private String lastModifiedUserName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLineSupplier() {
        return lineSupplier;
    }

    public void setLineSupplier(String lineSupplier) {
        this.lineSupplier = lineSupplier == null ? null : lineSupplier.trim();
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName == null ? null : lineName.trim();
    }

    public String getEffectDate() {
        return effectDate;
    }

    public void setEffectDate(String effectDate) {
        this.effectDate = effectDate == null ? null : effectDate.trim();
    }

    public String getIsCalcCost() {
        return isCalcCost;
    }

    public void setIsCalcCost(String isCalcCost) {
        this.isCalcCost = isCalcCost == null ? null : isCalcCost.trim();
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price == null ? null : price.trim();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime == null ? null : createdTime.trim();
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy == null ? null : lastModifiedBy.trim();
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime == null ? null : lastModifiedTime.trim();
    }

    public String getLastModifiedUserId() {
        return lastModifiedUserId;
    }

    public void setLastModifiedUserId(String lastModifiedUserId) {
        this.lastModifiedUserId = lastModifiedUserId == null ? null : lastModifiedUserId.trim();
    }

    public String getLastModifiedUserName() {
        return lastModifiedUserName;
    }

    public void setLastModifiedUserName(String lastModifiedUserName) {
        this.lastModifiedUserName = lastModifiedUserName == null ? null : lastModifiedUserName.trim();
    }
}