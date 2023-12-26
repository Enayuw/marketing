package com.br.marketing.entity;

import java.util.Date;

public class HaierCollidingDataLog {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private Long collidingDataId;

    /**
     * 
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 类型
     */
    private String type;

    /**
     * 手机号
     */
    private String mobileDigest;

    /**
     * 核验结果 1：营销，0：不营销
     */
    private Integer result;

    /**
     * 上传状态 1:待上传,2:已上传
     */
    private Integer syncStatus;

    /**
     * 状态 0-正常1-待推送 2-推送完成 3-异常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 推送日期
     */
    private Integer sendDate;

    /**
     * 扩展字段
     */
    private String extend;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCollidingDataId() {
        return collidingDataId;
    }

    public void setCollidingDataId(Long collidingDataId) {
        this.collidingDataId = collidingDataId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getMobileDigest() {
        return mobileDigest;
    }

    public void setMobileDigest(String mobileDigest) {
        this.mobileDigest = mobileDigest == null ? null : mobileDigest.trim();
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Integer getSendDate() {
        return sendDate;
    }

    public void setSendDate(Integer sendDate) {
        this.sendDate = sendDate;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}