package com.br.marketing.entity;

import java.util.Date;

public class XiechengCollidingDataPackageRuleStaging {
    /**
     * 
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 携程撞库包的id
     */
    private Long packageId;

    /**
     * 撞库数据清洗任务id
     */
    private Long collidingDataTaskId;

    /**
     * 撞得量级
     */
    private Integer collidingBackNumber;

    /**
     * 规则开始时间
     */
    private Date collidingStartTime;

    /**
     * 规则结束时间
     */
    private Date collidingEndTime;

    /**
     * 撞库开始时间（多个时间以逗号分割，格式HH:mm）
     */
    private String startTimes;

    /**
     * 一天内的撞库次数
     */
    private Integer collidingTimes;

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

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Long getCollidingDataTaskId() {
        return collidingDataTaskId;
    }

    public void setCollidingDataTaskId(Long collidingDataTaskId) {
        this.collidingDataTaskId = collidingDataTaskId;
    }

    public Integer getCollidingBackNumber() {
        return collidingBackNumber;
    }

    public void setCollidingBackNumber(Integer collidingBackNumber) {
        this.collidingBackNumber = collidingBackNumber;
    }

    public Date getCollidingStartTime() {
        return collidingStartTime;
    }

    public void setCollidingStartTime(Date collidingStartTime) {
        this.collidingStartTime = collidingStartTime;
    }

    public Date getCollidingEndTime() {
        return collidingEndTime;
    }

    public void setCollidingEndTime(Date collidingEndTime) {
        this.collidingEndTime = collidingEndTime;
    }

    public String getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(String startTimes) {
        this.startTimes = startTimes == null ? null : startTimes.trim();
    }

    public Integer getCollidingTimes() {
        return collidingTimes;
    }

    public void setCollidingTimes(Integer collidingTimes) {
        this.collidingTimes = collidingTimes;
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