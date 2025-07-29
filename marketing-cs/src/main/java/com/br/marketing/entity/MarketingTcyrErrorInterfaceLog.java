package com.br.marketing.entity;

import java.util.Date;

public class MarketingTcyrErrorInterfaceLog {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 同一批次下具体单个文件的id
     */
    private Long syncFileId;

    /**
     * 批次请求对应的元素个数
     */
    private Integer elementCount;

    /**
     * 异常请求处理状态  0-未处理 1-中间态 2:处理成功
     */
    private Integer dealStatus;

    /**
     * 删除状态 1-可用 9-删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 批次请求的具体参数
     */
    private String requestParam;

    /**
     * 上传接口返回的失败结果
     */
    private String pushResult;

    /**
     * 错误类型 1:返回错误 2:请求异常 3:二次处理异常
     */
    private Integer errorType;

    /**
     * 请求的request_id
     */
    private String requestId;

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

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public Long getSyncFileId() {
        return syncFileId;
    }

    public void setSyncFileId(Long syncFileId) {
        this.syncFileId = syncFileId;
    }

    public Integer getElementCount() {
        return elementCount;
    }

    public void setElementCount(Integer elementCount) {
        this.elementCount = elementCount;
    }

    public Integer getDealStatus() {
        return dealStatus;
    }

    public void setDealStatus(Integer dealStatus) {
        this.dealStatus = dealStatus;
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

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam == null ? null : requestParam.trim();
    }

    public String getPushResult() {
        return pushResult;
    }

    public void setPushResult(String pushResult) {
        this.pushResult = pushResult == null ? null : pushResult.trim();
    }

    public Integer getErrorType() {
        return errorType;
    }

    public void setErrorType(Integer errorType) {
        this.errorType = errorType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }
}