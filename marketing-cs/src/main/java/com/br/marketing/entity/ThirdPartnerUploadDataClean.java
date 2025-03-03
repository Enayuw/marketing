package com.br.marketing.entity;

import java.util.Date;

public class ThirdPartnerUploadDataClean {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 请求流水号
     */
    private String accessNumber;

    /**
     * 客户编号
     */
    private String apiCode;

    /**
     * 原始客户编号
     */
    private String orgApiCode;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 场景
     */
    private String userType;

    /**
     * 子场景
     */
    private String customNameType;

    /**
     * 三方类型 1-百应、2-百可录
     */
    private Integer resourceChannel;

    /**
     * 生效开始日期
     */
    private String validStartDate;

    /**
     * 生效结束日期
     */
    private String validEndDate;

    /**
     * 清洗状态 0-待清洗 1-清洗中，2-已完成
     */
    private Integer cleanStatus;

    /**
     * 清洗任务id
     */
    private Long taskId;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDeleted;

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

    public String getAccessNumber() {
        return accessNumber;
    }

    public void setAccessNumber(String accessNumber) {
        this.accessNumber = accessNumber == null ? null : accessNumber.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getOrgApiCode() {
        return orgApiCode;
    }

    public void setOrgApiCode(String orgApiCode) {
        this.orgApiCode = orgApiCode == null ? null : orgApiCode.trim();
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum == null ? null : custNum.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getCustomNameType() {
        return customNameType;
    }

    public void setCustomNameType(String customNameType) {
        this.customNameType = customNameType == null ? null : customNameType.trim();
    }

    public Integer getResourceChannel() {
        return resourceChannel;
    }

    public void setResourceChannel(Integer resourceChannel) {
        this.resourceChannel = resourceChannel;
    }

    public String getValidStartDate() {
        return validStartDate;
    }

    public void setValidStartDate(String validStartDate) {
        this.validStartDate = validStartDate == null ? null : validStartDate.trim();
    }

    public String getValidEndDate() {
        return validEndDate;
    }

    public void setValidEndDate(String validEndDate) {
        this.validEndDate = validEndDate == null ? null : validEndDate.trim();
    }

    public Integer getCleanStatus() {
        return cleanStatus;
    }

    public void setCleanStatus(Integer cleanStatus) {
        this.cleanStatus = cleanStatus;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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