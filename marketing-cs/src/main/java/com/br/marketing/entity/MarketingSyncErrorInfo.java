package com.br.marketing.entity;

import java.util.Date;

public class MarketingSyncErrorInfo {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 客户批次号
     */
    private String cusBatch;

    /**
     * 请求批次号
     */
    private String requestBatch;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 错误信息
     */
    private String errorInfo;

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

    public String getCusBatch() {
        return cusBatch;
    }

    public void setCusBatch(String cusBatch) {
        this.cusBatch = cusBatch == null ? null : cusBatch.trim();
    }

    public String getRequestBatch() {
        return requestBatch;
    }

    public void setRequestBatch(String requestBatch) {
        this.requestBatch = requestBatch == null ? null : requestBatch.trim();
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

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo == null ? null : errorInfo.trim();
    }
}