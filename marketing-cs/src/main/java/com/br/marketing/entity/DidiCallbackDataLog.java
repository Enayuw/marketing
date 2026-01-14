package com.br.marketing.entity;

import java.util.Date;

public class DidiCallbackDataLog {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 对应回推表id
     */
    private Long callbackId;

    /**
     * API代码
     */
    private String apiCode;

    /**
     * 案件编号/客户号码
     */
    private String cell;

    /**
     * 请求码
     */
    private String httpCode;

    /**
     * 业务异常码
     */
    private String errorCode;

    /**
     * 业务异常信息
     */
    private String errorMessage;

    /**
     * 接口返回内容
     */
    private String returnContent;

    /**
     * 推送类型:1-触达成功数据,0-触达失败数据,2-通话失败改成功,3-短信失败改成功
     */
    private Integer pushType;

    /**
     * 推送状态：1-成功，0-失败
     */
    private Integer pushStatus;

    /**
     * meidaName
     */
    private String meidaName;

    /**
     * scas
     */
    private String scas;

    /**
     * timestamp
     */
    private String timestamp;

    /**
     * signature
     */
    private String signature;

    /**
     * channelId
     */
    private String channelId;

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

    public Long getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(Long callbackId) {
        this.callbackId = callbackId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode == null ? null : httpCode.trim();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode == null ? null : errorCode.trim();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? null : errorMessage.trim();
    }

    public String getReturnContent() {
        return returnContent;
    }

    public void setReturnContent(String returnContent) {
        this.returnContent = returnContent == null ? null : returnContent.trim();
    }

    public Integer getPushType() {
        return pushType;
    }

    public void setPushType(Integer pushType) {
        this.pushType = pushType;
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    public String getMeidaName() {
        return meidaName;
    }

    public void setMeidaName(String meidaName) {
        this.meidaName = meidaName == null ? null : meidaName.trim();
    }

    public String getScas() {
        return scas;
    }

    public void setScas(String scas) {
        this.scas = scas == null ? null : scas.trim();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp == null ? null : timestamp.trim();
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature == null ? null : signature.trim();
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId == null ? null : channelId.trim();
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