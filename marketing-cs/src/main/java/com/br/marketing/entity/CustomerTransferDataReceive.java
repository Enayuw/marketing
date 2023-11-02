package com.br.marketing.entity;

import java.util.Date;

public class CustomerTransferDataReceive {
    /**
     *
     */
    private Long id;

    /**
     * 用户编号
     */
    private String apiCode;

    /**
     * 请求流水号
     */
    private String requestId;

    /**
     * 请求数据
     */
    private String requestJsonData;

    /**
     * 数据量
     */
    private Integer bizDataNumber;

    /**
     * 响应码
     */
    private String responseCode;

    /**
     * 响应数据
     */
    private String responseData;

    /**
     * 状态 0-无效、1-有效
     */
    private Integer status;

    /**
     * 同步状态 0-失败、1-成功
     */
    private Integer syncStatus;

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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getRequestJsonData() {
        return requestJsonData;
    }

    public void setRequestJsonData(String requestJsonData) {
        this.requestJsonData = requestJsonData == null ? null : requestJsonData.trim();
    }

    public Integer getBizDataNumber() {
        return bizDataNumber;
    }

    public void setBizDataNumber(Integer bizDataNumber) {
        this.bizDataNumber = bizDataNumber;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode == null ? null : responseCode.trim();
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData == null ? null : responseData.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
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