package com.br.marketing.entity;

import java.util.Date;

public class RetryDetailLog {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 主表id
     */
    private Long mainId;

    /**
     * 重试结果
     */
    private String retryResult;

    /**
     * 重试状态 1-成功；2-失败
     */
    private Integer retryStatus;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMainId() {
        return mainId;
    }

    public void setMainId(Long mainId) {
        this.mainId = mainId;
    }

    public String getRetryResult() {
        return retryResult;
    }

    public void setRetryResult(String retryResult) {
        this.retryResult = retryResult == null ? null : retryResult.trim();
    }

    public Integer getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(Integer retryStatus) {
        this.retryStatus = retryStatus;
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