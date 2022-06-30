package com.br.marketing.entity;

import java.util.Date;

public class TransferSyncReport {
    /**
     *
     */
    private Long id;

    /**
     * 客户编号
     */
    private String cid;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 客户名称
     */
    private String shortName;

    /**
     * 上传日期
     */
    private String appletDate;

    /**
     * 场景
     */
    private String userType;

    /**
     * 转化数据量
     */
    private Integer dataCount;

    /**
     * 上传开始时间
     */
    private Date appletBeginTime;

    /**
     * 上传结束时间
     */
    private Date appletEndTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName == null ? null : shortName.trim();
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public Integer getDataCount() {
        return dataCount;
    }

    public void setDataCount(Integer dataCount) {
        this.dataCount = dataCount;
    }

    public Date getAppletBeginTime() {
        return appletBeginTime;
    }

    public void setAppletBeginTime(Date appletBeginTime) {
        this.appletBeginTime = appletBeginTime;
    }

    public Date getAppletEndTime() {
        return appletEndTime;
    }

    public void setAppletEndTime(Date appletEndTime) {
        this.appletEndTime = appletEndTime;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    @Override
    public String toString() {
        return "TransferSyncReport{" +
                "id=" + id +
                ", cid='" + cid + '\'' +
                ", apiCode='" + apiCode + '\'' +
                ", shortName='" + shortName + '\'' +
                ", appletDate='" + appletDate + '\'' +
                ", userType='" + userType + '\'' +
                ", dataCount=" + dataCount +
                ", appletBeginTime=" + appletBeginTime +
                ", appletEndTime=" + appletEndTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", remark='" + remark + '\'' +
                '}';
    }
}