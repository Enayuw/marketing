package com.br.marketing.entity;

import java.util.Date;

public class MarketingSyncReport {
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
     * 数据正常入库条数
     */
    private Integer normalNum;

    /**
     * 去重后数据量
     */
    private Integer duplicateRemovalNum;

    /**
     * 上传开始时间
     */
    private Date appletBeginTime;

    /**
     * 上传结束时间
     */
    private Date appletEndTime;

    /**
     * 扩展字段中key的集合
     */
    private String reserveField1Key;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 标签信息json结构
     */
    private String labelMessage;

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

    public Integer getNormalNum() {
        return normalNum;
    }

    public void setNormalNum(Integer normalNum) {
        this.normalNum = normalNum;
    }

    public Integer getDuplicateRemovalNum() {
        return duplicateRemovalNum;
    }

    public void setDuplicateRemovalNum(Integer duplicateRemovalNum) {
        this.duplicateRemovalNum = duplicateRemovalNum;
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

    public String getReserveField1Key() {
        return reserveField1Key;
    }

    public void setReserveField1Key(String reserveField1Key) {
        this.reserveField1Key = reserveField1Key == null ? null : reserveField1Key.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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

    public String getLabelMessage() {
        return labelMessage;
    }

    public void setLabelMessage(String labelMessage) {
        this.labelMessage = labelMessage == null ? null : labelMessage.trim();
    }
}