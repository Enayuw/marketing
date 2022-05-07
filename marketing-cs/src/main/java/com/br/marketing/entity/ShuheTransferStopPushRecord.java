package com.br.marketing.entity;

import java.util.Date;

public class ShuheTransferStopPushRecord {
    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 用户唯一编号
     */
    private String caseNum;

    /**
     * 场景
     */
    private String userType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 失效时间
     */
    private Date failureTime;

    /**
     * 拨打记录id，两个及以上时以","分隔
     */
    private String callRecordId;

    /**
     * 转化详情id
     */
    private String transferSyncCidId;

    /**
     * 天
     */
    private String day;

    /**
     * 0 电销；
     */
    private Integer channel;

    /**
     * 情况
     */
    private String status;

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

    public String getCaseNum() {
        return caseNum;
    }

    public void setCaseNum(String caseNum) {
        this.caseNum = caseNum == null ? null : caseNum.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
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

    public Date getFailureTime() {
        return failureTime;
    }

    public void setFailureTime(Date failureTime) {
        this.failureTime = failureTime;
    }

    public String getCallRecordId() {
        return callRecordId;
    }

    public void setCallRecordId(String callRecordId) {
        this.callRecordId = callRecordId == null ? null : callRecordId.trim();
    }

    public String getTransferSyncCidId() {
        return transferSyncCidId;
    }

    public void setTransferSyncCidId(String transferSyncCidId) {
        this.transferSyncCidId = transferSyncCidId == null ? null : transferSyncCidId.trim();
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day == null ? null : day.trim();
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    @Override
    public String toString() {
        return "ShuheTransferStopPushRecord{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", caseNum='" + caseNum + '\'' +
                ", userType='" + userType + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", failureTime=" + failureTime +
                ", callRecordId='" + callRecordId + '\'' +
                ", transferSyncCidId='" + transferSyncCidId + '\'' +
                ", day='" + day + '\'' +
                ", channel=" + channel +
                ", status='" + status + '\'' +
                '}';
    }
}