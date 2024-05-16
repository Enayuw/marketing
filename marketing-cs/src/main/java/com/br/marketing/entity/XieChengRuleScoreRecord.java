package com.br.marketing.entity;

import java.util.Date;

public class XieChengRuleScoreRecord {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 跑分任务编号
     */
    private String batchNumber;

    /**
     * 跑分文件名称
     */
    private String fileName;

    /**
     * 明细状态 1：数据同步中，2 同步完成
     */
    private Integer recordStatus;

    /**
     * 去重后实际数量
     */
    private Integer actualNumber;

    /**
     * 
     */
    private String extend;

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public Integer getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(Integer recordStatus) {
        this.recordStatus = recordStatus;
    }

    public Integer getActualNumber() {
        return actualNumber;
    }

    public void setActualNumber(Integer actualNumber) {
        this.actualNumber = actualNumber;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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