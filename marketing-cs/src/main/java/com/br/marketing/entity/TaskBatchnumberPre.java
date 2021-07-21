package com.br.marketing.entity;

import java.util.Date;

public class TaskBatchnumberPre {
    /**
     * 
     */
    private Long id;

    /**
     * 客户编号
     */
    private String apiCode;

    /**
     * 客户批次号
     */
    private String cusBatch;

    /**
     * 场景
     */
    private String groupType;

    /**
     * 执行时间（yyyy-MM-dd）
     */
    private String recordDate;

    /**
     * 策略id
     */
    private String strategyId;

    /**
     * 内部批次号
     */
    private String batchNumber;

    /**
     * 有效标志 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 1-未使用；2-已使用
     */
    private Integer status;

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

    public String getCusBatch() {
        return cusBatch;
    }

    public void setCusBatch(String cusBatch) {
        this.cusBatch = cusBatch == null ? null : cusBatch.trim();
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType == null ? null : groupType.trim();
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate == null ? null : recordDate.trim();
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId == null ? null : strategyId.trim();
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}