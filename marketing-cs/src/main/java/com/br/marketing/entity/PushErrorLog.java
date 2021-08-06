package com.br.marketing.entity;

import java.util.Date;

public class PushErrorLog {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * stra_his_file表 id
     */
    private Long fileId;

    /**
     * 推送状态  1成功 2失败
     */
    private Integer status;

    /**
     * 请求数据
     */
    private String requestStr;

    /**
     * 响应数据
     */
    private String responseStr;

    /**
     * 重试次数
     */
    private Integer pushTimes;

    /**
     * 实际重试次数
     */
    private Integer actualPushTimes;

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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRequestStr() {
        return requestStr;
    }

    public void setRequestStr(String requestStr) {
        this.requestStr = requestStr == null ? null : requestStr.trim();
    }

    public String getResponseStr() {
        return responseStr;
    }

    public void setResponseStr(String responseStr) {
        this.responseStr = responseStr == null ? null : responseStr.trim();
    }

    public Integer getPushTimes() {
        return pushTimes;
    }

    public void setPushTimes(Integer pushTimes) {
        this.pushTimes = pushTimes;
    }

    public Integer getActualPushTimes() {
        return actualPushTimes;
    }

    public void setActualPushTimes(Integer actualPushTimes) {
        this.actualPushTimes = actualPushTimes;
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