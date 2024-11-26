package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * b_marketing_sync_report
 * @author 
 */
public class MarketingSyncReport implements Serializable {
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

    private static final long serialVersionUID = 1L;

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
        this.cid = cid;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
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
        this.reserveField1Key = reserveField1Key;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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