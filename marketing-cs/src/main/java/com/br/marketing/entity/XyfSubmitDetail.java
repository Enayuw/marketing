package com.br.marketing.entity;

import java.util.Date;

public class XyfSubmitDetail {
    /**
     * 主键
     */
    private Long id;

    /**
     * 请求批次编号
     */
    private String batchId;

    /**
     * 工单编号
     */
    private String jobId;

    /**
     * 需要呼叫的手机号
     */
    private String phone;

    /**
     * 前缀拼接手机号
     */
    private String prePhone;

    /**
     * 产品类型
     */
    private String productType;

    /**
     * 工作数据 完整内容
     */
    private String jobData;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 是否上传
     */
    private Integer isSync;

    /**
     * 删除状态 1-可用 9-删除
     */
    private Integer isDel;

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

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId == null ? null : batchId.trim();
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId == null ? null : jobId.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getPrePhone() {
        return prePhone;
    }

    public void setPrePhone(String prePhone) {
        this.prePhone = prePhone == null ? null : prePhone.trim();
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType == null ? null : productType.trim();
    }

    public String getJobData() {
        return jobData;
    }

    public void setJobData(String jobData) {
        this.jobData = jobData == null ? null : jobData.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public Integer getIsSync() {
        return isSync;
    }

    public void setIsSync(Integer isSync) {
        this.isSync = isSync;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}