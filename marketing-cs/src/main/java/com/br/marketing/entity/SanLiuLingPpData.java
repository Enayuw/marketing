package com.br.marketing.entity;

import java.util.Date;

public class SanLiuLingPpData {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * request_id
     */
    private String requestId;

    /**
     * 手机号码
     */
    private String mobileNoMd5;

    /**
     * 投放结果
     */
    private String mobileResult;

    /**
     * 拓展字段间隔符
     */
    private String extend;

    /**
     * 状态 1-未推送；2-推送成功；3-推送失败；4-匹配失败
     */
    private Integer pushStatus;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 描述-推送异常会记录异常信息
     */
    private String dataMessage;

    /**
     * 日期
     */
    private Integer createDate;

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

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getMobileNoMd5() {
        return mobileNoMd5;
    }

    public void setMobileNoMd5(String mobileNoMd5) {
        this.mobileNoMd5 = mobileNoMd5 == null ? null : mobileNoMd5.trim();
    }

    public String getMobileResult() {
        return mobileResult;
    }

    public void setMobileResult(String mobileResult) {
        this.mobileResult = mobileResult == null ? null : mobileResult.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDataMessage() {
        return dataMessage;
    }

    public void setDataMessage(String dataMessage) {
        this.dataMessage = dataMessage == null ? null : dataMessage.trim();
    }

    public Integer getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Integer createDate) {
        this.createDate = createDate;
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