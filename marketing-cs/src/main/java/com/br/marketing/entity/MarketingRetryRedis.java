package com.br.marketing.entity;

import java.util.Date;

public class MarketingRetryRedis {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 跑分记录
     */
    private String batchNumber;

    /**
     * 跑分页码
     */
    private String page;

    /**
     * 跑分执行记录redisKey
     */
    private String redisKey;

    /**
     * 跑分执行记录redisValue
     */
    private String redisValue;

    /**
     * 跑分执行记录redisValue类型
     */
    private String redisValueType;

    /**
     * 重试状态 0-待重试；1-重试成功；2-重试失败
     */
    private Integer retryStatus;

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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page == null ? null : page.trim();
    }

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey == null ? null : redisKey.trim();
    }

    public String getRedisValue() {
        return redisValue;
    }

    public void setRedisValue(String redisValue) {
        this.redisValue = redisValue == null ? null : redisValue.trim();
    }

    public String getRedisValueType() {
        return redisValueType;
    }

    public void setRedisValueType(String redisValueType) {
        this.redisValueType = redisValueType == null ? null : redisValueType.trim();
    }

    public Integer getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(Integer retryStatus) {
        this.retryStatus = retryStatus;
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