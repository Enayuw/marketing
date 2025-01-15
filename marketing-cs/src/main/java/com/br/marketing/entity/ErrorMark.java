package com.br.marketing.entity;

import java.util.Date;

public class ErrorMark {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 任务id
     */
    private Long mId;

    /**
     * es分片数
     */
    private String part;

    /**
     * es查询页码
     */
    private Integer pageSize;

    /**
     * 
     */
    private String searchAfter;

    /**
     * 查询es条件
     */
    private String esCondition;

    /**
     * 推决策请求唯一标识
     */
    private String accessNumber;

    /**
     * 推决策条数
     */
    private Integer pushSize;

    /**
     * 推决策条件
     */
    private String policyCondition;

    /**
     * 补推次数
     */
    private Integer retryTotalAttempts;

    /**
     * 补推状态 0-待补推；1-补推完成
     */
    private Integer retryStatus;

    /**
     * 任务类型 0 跑分数据推决策, 1 跑分及撞库结果筛选推决策
     */
    private Integer filterType;

    /**
     * 异常类型 0-查询ES异常 1-推送决策异常
     */
    private Integer type;

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

    public Long getmId() {
        return mId;
    }

    public void setmId(Long mId) {
        this.mId = mId;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part == null ? null : part.trim();
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSearchAfter() {
        return searchAfter;
    }

    public void setSearchAfter(String searchAfter) {
        this.searchAfter = searchAfter == null ? null : searchAfter.trim();
    }

    public String getEsCondition() {
        return esCondition;
    }

    public void setEsCondition(String esCondition) {
        this.esCondition = esCondition == null ? null : esCondition.trim();
    }

    public String getAccessNumber() {
        return accessNumber;
    }

    public void setAccessNumber(String accessNumber) {
        this.accessNumber = accessNumber == null ? null : accessNumber.trim();
    }

    public Integer getPushSize() {
        return pushSize;
    }

    public void setPushSize(Integer pushSize) {
        this.pushSize = pushSize;
    }

    public String getPolicyCondition() {
        return policyCondition;
    }

    public void setPolicyCondition(String policyCondition) {
        this.policyCondition = policyCondition == null ? null : policyCondition.trim();
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

    public Integer getFilterType() {
        return filterType;
    }

    public void setFilterType(Integer filterType) {
        this.filterType = filterType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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