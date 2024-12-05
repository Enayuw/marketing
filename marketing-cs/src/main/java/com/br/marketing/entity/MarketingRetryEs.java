package com.br.marketing.entity;

import java.util.Date;

public class MarketingRetryEs {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 跑分记录id
     */
    private String fileId;

    /**
     * 重试次数
     */
    private Integer retryTotalAttempts;

    /**
     * 重试状态 0-待重试；1-重试中；2-重试成功；3-重试失败
     */
    private Integer retryStatus;

    /**
     * 预留字段1
     */
    private String reserveField1;

    /**
     * 执行日期
     */
    private String appletDate;

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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId == null ? null : fileId.trim();
    }

    public Integer getRetryTotalAttempts() {
        return retryTotalAttempts;
    }

    public void setRetryTotalAttempts(Integer retryTotalAttempts) {
        this.retryTotalAttempts = retryTotalAttempts;
    }

    public Integer getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(Integer retryStatus) {
        this.retryStatus = retryStatus;
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1 == null ? null : reserveField1.trim();
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
}