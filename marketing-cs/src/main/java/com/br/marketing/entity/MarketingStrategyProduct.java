package com.br.marketing.entity;

import java.util.Date;

public class MarketingStrategyProduct {
    /**
     * 逐渐id
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 
     */
    private String cusBatchNumber;

    /**
     * 策略id
     */
    private String strategyId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品版本
     */
    private String productVersion;

    /**
     * 1-有效；9-无效；
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

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

    public String getCusBatchNumber() {
        return cusBatchNumber;
    }

    public void setCusBatchNumber(String cusBatchNumber) {
        this.cusBatchNumber = cusBatchNumber == null ? null : cusBatchNumber.trim();
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId == null ? null : strategyId.trim();
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName == null ? null : productName.trim();
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion == null ? null : productVersion.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}