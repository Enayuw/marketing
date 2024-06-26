package com.br.marketing.entity;

import java.util.Date;

public class QueryUserRealMessage {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 用户编号
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 响应结果
     */
    private String respData;

    /**
     * 唯一号
     */
    private String uniqueReqNo;

    /**
     * 手机号
     */
    private String mobileMd5;

    /**
     * 营销信号 Y 停止营销 N 可营销
     */
    private String stopMarketingSign;

    /**
     * 用户完件信息
     */
    private String userMessage;

    /**
     * 授信信息
     */
    private String riskMessage;

    /**
     * 交易信息
     */
    private String tradeMessage;

    /**
     * 推送状态：0- 待推送, 1-推送中，2推送成功，3-推送失败
     */
    private Integer status;

    /**
     * 错误原因
     */
    private String errorMsg;

    /**
     * 创建日期
     */
    private String createDate;

    /**
     * 是否删除 0:否;1:是;
     */
    private Integer isDeleted;

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

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public String getRespData() {
        return respData;
    }

    public void setRespData(String respData) {
        this.respData = respData == null ? null : respData.trim();
    }

    public String getUniqueReqNo() {
        return uniqueReqNo;
    }

    public void setUniqueReqNo(String uniqueReqNo) {
        this.uniqueReqNo = uniqueReqNo == null ? null : uniqueReqNo.trim();
    }

    public String getMobileMd5() {
        return mobileMd5;
    }

    public void setMobileMd5(String mobileMd5) {
        this.mobileMd5 = mobileMd5 == null ? null : mobileMd5.trim();
    }

    public String getStopMarketingSign() {
        return stopMarketingSign;
    }

    public void setStopMarketingSign(String stopMarketingSign) {
        this.stopMarketingSign = stopMarketingSign == null ? null : stopMarketingSign.trim();
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage == null ? null : userMessage.trim();
    }

    public String getRiskMessage() {
        return riskMessage;
    }

    public void setRiskMessage(String riskMessage) {
        this.riskMessage = riskMessage == null ? null : riskMessage.trim();
    }

    public String getTradeMessage() {
        return tradeMessage;
    }

    public void setTradeMessage(String tradeMessage) {
        this.tradeMessage = tradeMessage == null ? null : tradeMessage.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg == null ? null : errorMsg.trim();
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate == null ? null : createDate.trim();
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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