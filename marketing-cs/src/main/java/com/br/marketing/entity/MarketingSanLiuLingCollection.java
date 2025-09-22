package com.br.marketing.entity;

import java.util.Date;

public class MarketingSanLiuLingCollection {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 客户号
     */
    private String applicationId;

    /**
     * 客户手机号码 加密 AES加密
     */
    private String phone;

    /**
     * 语音合成参数Json字符串
     */
    private String speechParamSet;

    /**
     * 客户姓名 加密 AES加密
     */
    private String customerName;

    /**
     * 案件号
     */
    private String caseCode;

    /**
     * 产品类型
     */
    private String productType;

    /**
     * 催收名义
     */
    private String prologueRemark;

    /**
     * br1, br2, lxr1, lxr2, bn1, bn2
     */
    private String phoneLabel;

    /**
     * 清洗状态：0待清洗、1清洗中、2清洗完成
     */
    private Integer cleanStatus;

    /**
     * 0-正常(未删除状态) ，1-删除状态 (默认0)
     */
    private Integer isDelete;

    /**
     * 接入日期yyyy-MM-dd
     */
    private String receiveDate;

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

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId == null ? null : applicationId.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getSpeechParamSet() {
        return speechParamSet;
    }

    public void setSpeechParamSet(String speechParamSet) {
        this.speechParamSet = speechParamSet == null ? null : speechParamSet.trim();
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName == null ? null : customerName.trim();
    }

    public String getCaseCode() {
        return caseCode;
    }

    public void setCaseCode(String caseCode) {
        this.caseCode = caseCode == null ? null : caseCode.trim();
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType == null ? null : productType.trim();
    }

    public String getPrologueRemark() {
        return prologueRemark;
    }

    public void setPrologueRemark(String prologueRemark) {
        this.prologueRemark = prologueRemark == null ? null : prologueRemark.trim();
    }

    public String getPhoneLabel() {
        return phoneLabel;
    }

    public void setPhoneLabel(String phoneLabel) {
        this.phoneLabel = phoneLabel == null ? null : phoneLabel.trim();
    }

    public Integer getCleanStatus() {
        return cleanStatus;
    }

    public void setCleanStatus(Integer cleanStatus) {
        this.cleanStatus = cleanStatus;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate == null ? null : receiveDate.trim();
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