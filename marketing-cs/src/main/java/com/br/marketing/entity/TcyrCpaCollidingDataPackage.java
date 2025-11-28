package com.br.marketing.entity;

import java.util.Date;

public class TcyrCpaCollidingDataPackage {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 包名称
     */
    private String packageName;

    /**
     * 跑分任务编号
     */
    private String batchNumbers;

    /**
     * 跑分条件
     */
    private String conditions;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 量级
     */
    private Integer magnitude;

    /**
     * 禁用标志 0-禁用 1-启用
     */
    private Integer enabled;

    /**
     * 清洗状态 0-待清洗；1-清洗中；2-清洗完成；3-清洗失败
     */
    private Integer cleanStatus;

    /**
     * 执行信息
     */
    private String executeInfo;

    /**
     * 删除状态 1-可用；2-删除中；9-删除
     */
    private Integer isDel;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建时间
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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? null : packageName.trim();
    }

    public String getBatchNumbers() {
        return batchNumbers;
    }

    public void setBatchNumbers(String batchNumbers) {
        this.batchNumbers = batchNumbers == null ? null : batchNumbers.trim();
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions == null ? null : conditions.trim();
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Integer magnitude) {
        this.magnitude = magnitude;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getCleanStatus() {
        return cleanStatus;
    }

    public void setCleanStatus(Integer cleanStatus) {
        this.cleanStatus = cleanStatus;
    }

    public String getExecuteInfo() {
        return executeInfo;
    }

    public void setExecuteInfo(String executeInfo) {
        this.executeInfo = executeInfo == null ? null : executeInfo.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
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