package com.br.marketing.entity;

import java.util.Date;

public class XiechengCollidingTaskBatch {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 撞库数据清洗任务id
     */
    private Long collidingDataTaskId;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 剔除类型，1-周期数据剔除、3-动态补充包剔除
     */
    private Integer type;

    /**
     * 0-未剔除，1-剔除中，2-剔除完成
     */
    private Integer status;

    /**
     * 数据量级
     */
    private Integer actualNumber;

    /**
     * 0-正常；1-删除
     */
    private Integer isDelete;

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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Long getCollidingDataTaskId() {
        return collidingDataTaskId;
    }

    public void setCollidingDataTaskId(Long collidingDataTaskId) {
        this.collidingDataTaskId = collidingDataTaskId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getActualNumber() {
        return actualNumber;
    }

    public void setActualNumber(Integer actualNumber) {
        this.actualNumber = actualNumber;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
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