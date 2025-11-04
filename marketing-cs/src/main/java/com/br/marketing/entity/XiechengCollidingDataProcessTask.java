package com.br.marketing.entity;

import java.util.Date;

public class XiechengCollidingDataProcessTask {
    /**
     * 
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 跑分任务编号
     */
    private String batchNumber;

    /**
     * 任务状态 0：任务待代行，1 任务执行中，2 任务执行完成
     */
    private Integer taskStatus;

    /**
     * 预估数据量级
     */
    private Integer discreetNumber;

    /**
     * 实际数据数量
     */
    private Integer actualNumber;

    /**
     * 剩余量级
     */
    private Integer remainingNum;

    /**
     * 空挡量级
     */
    private Integer freeNum;

    /**
     * 任务执行开始时间
     */
    private Date taskStartTime;

    /**
     * 任务执行结束时间
     */
    private Date taskEndTime;

    /**
     * 任务执行时间
     */
    private Date taskExecuteTime;

    /**
     * 施放时间范围开始时间
     */
    private Date releaseTimeBegin;

    /**
     * 施放时间范围结束时间
     */
    private Date releaseTimeEnd;

    /**
     * 任务类型 0-非周期数据清洗，1-周期数据剔除，2-推送决策，3-false动态包剔除，4-黑名单剔除
     */
    private Integer taskType;

    /**
     * 预估量级执行条件
     */
    private String taskExecutionConditions;

    /**
     * 预估量级执行sql
     */
    private String taskExecutionSql;

    /**
     * 异常信息详情
     */
    private String errorMessage;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 0 正常，1删除
     */
    private Integer isDelete;

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

    public Integer getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Integer taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getDiscreetNumber() {
        return discreetNumber;
    }

    public void setDiscreetNumber(Integer discreetNumber) {
        this.discreetNumber = discreetNumber;
    }

    public Integer getActualNumber() {
        return actualNumber;
    }

    public void setActualNumber(Integer actualNumber) {
        this.actualNumber = actualNumber;
    }

    public Integer getRemainingNum() {
        return remainingNum;
    }

    public void setRemainingNum(Integer remainingNum) {
        this.remainingNum = remainingNum;
    }

    public Integer getFreeNum() {
        return freeNum;
    }

    public void setFreeNum(Integer freeNum) {
        this.freeNum = freeNum;
    }

    public Date getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(Date taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public Date getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(Date taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public Date getTaskExecuteTime() {
        return taskExecuteTime;
    }

    public void setTaskExecuteTime(Date taskExecuteTime) {
        this.taskExecuteTime = taskExecuteTime;
    }

    public Date getReleaseTimeBegin() {
        return releaseTimeBegin;
    }

    public void setReleaseTimeBegin(Date releaseTimeBegin) {
        this.releaseTimeBegin = releaseTimeBegin;
    }

    public Date getReleaseTimeEnd() {
        return releaseTimeEnd;
    }

    public void setReleaseTimeEnd(Date releaseTimeEnd) {
        this.releaseTimeEnd = releaseTimeEnd;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public String getTaskExecutionConditions() {
        return taskExecutionConditions;
    }

    public void setTaskExecutionConditions(String taskExecutionConditions) {
        this.taskExecutionConditions = taskExecutionConditions == null ? null : taskExecutionConditions.trim();
    }

    public String getTaskExecutionSql() {
        return taskExecutionSql;
    }

    public void setTaskExecutionSql(String taskExecutionSql) {
        this.taskExecutionSql = taskExecutionSql == null ? null : taskExecutionSql.trim();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? null : errorMessage.trim();
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

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}