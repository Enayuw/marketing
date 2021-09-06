package com.br.marketing.entity;

import java.util.Date;

public class SoleOptLog {
    /**
     * 
     */
    private Long id;

    /**
     * 去重规则id
     */
    private String soleId;

    /**
     * 操作人id
     */
    private String optUserId;

    /**
     * 操作人姓名
     */
    private String optUserName;

    /**
     * 删除标志；1-正常；9-删除；
     */
    private Integer isDel;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 去重规则名称
     */
    private String soleName;

    /**
     * 去重字段
     */
    private String soleFields;

    /**
     * 去重时间周期
     */
    private Integer soleCycleTimes;

    /**
     * 匹配商户
     */
    private String customerInfo;

    /**
     * 开启状态 1-开启；2-禁用；3-开启中
     */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSoleId() {
        return soleId;
    }

    public void setSoleId(String soleId) {
        this.soleId = soleId == null ? null : soleId.trim();
    }

    public String getOptUserId() {
        return optUserId;
    }

    public void setOptUserId(String optUserId) {
        this.optUserId = optUserId == null ? null : optUserId.trim();
    }

    public String getOptUserName() {
        return optUserName;
    }

    public void setOptUserName(String optUserName) {
        this.optUserName = optUserName == null ? null : optUserName.trim();
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

    public String getSoleName() {
        return soleName;
    }

    public void setSoleName(String soleName) {
        this.soleName = soleName == null ? null : soleName.trim();
    }

    public String getSoleFields() {
        return soleFields;
    }

    public void setSoleFields(String soleFields) {
        this.soleFields = soleFields == null ? null : soleFields.trim();
    }

    public Integer getSoleCycleTimes() {
        return soleCycleTimes;
    }

    public void setSoleCycleTimes(Integer soleCycleTimes) {
        this.soleCycleTimes = soleCycleTimes;
    }

    public String getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(String customerInfo) {
        this.customerInfo = customerInfo == null ? null : customerInfo.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}