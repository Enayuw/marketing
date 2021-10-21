package com.br.marketing.entity;

import java.util.Date;

public class TaskStatusDistribute {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 跑分记录表id
     */
    private Long fileId;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 开始id
     */
    private Long startId;

    /**
     * 分片索引
     */
    private Integer distributeIndex;

    /**
     * 结束id
     */
    private Long endId;

    /**
     * 预估数量
     */
    private Long preNum;

    /**
     * 实际数量
     */
    private Long actualNum;

    /**
     * 1-执行中，2-执行成功；3-执行失败
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

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

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public Long getStartId() {
        return startId;
    }

    public void setStartId(Long startId) {
        this.startId = startId;
    }

    public Integer getDistributeIndex() {
        return distributeIndex;
    }

    public void setDistributeIndex(Integer distributeIndex) {
        this.distributeIndex = distributeIndex;
    }

    public Long getEndId() {
        return endId;
    }

    public void setEndId(Long endId) {
        this.endId = endId;
    }

    public Long getPreNum() {
        return preNum;
    }

    public void setPreNum(Long preNum) {
        this.preNum = preNum;
    }

    public Long getActualNum() {
        return actualNum;
    }

    public void setActualNum(Long actualNum) {
        this.actualNum = actualNum;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }
}