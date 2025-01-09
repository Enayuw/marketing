package com.br.marketing.entity;

import java.util.Date;

public class XieChengCollidingDataRobTask {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 撞库包id
     */
    private Long packageId;

    /**
     * 规则表id
     */
    private Long ruleId;

    /**
     * 规则表撞得量级
     */
    private Integer collidingBackNumber;

    /**
     * 当前包的第几次撞库
     */
    private Integer packageCollidingCount;

    /**
     * 任务状态 0 待执行，1. 执行完成
     */
    private Integer taskStatus;

    /**
     * 撞库开始时间
     */
    private Date startTime;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Integer getCollidingBackNumber() {
        return collidingBackNumber;
    }

    public void setCollidingBackNumber(Integer collidingBackNumber) {
        this.collidingBackNumber = collidingBackNumber;
    }

    public Integer getPackageCollidingCount() {
        return packageCollidingCount;
    }

    public void setPackageCollidingCount(Integer packageCollidingCount) {
        this.packageCollidingCount = packageCollidingCount;
    }

    public Integer getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Integer taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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