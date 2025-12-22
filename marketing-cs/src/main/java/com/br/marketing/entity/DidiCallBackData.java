package com.br.marketing.entity;

import java.util.Date;

public class DidiCallBackData {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 回调参数类型:1-拨打结果,2-短信发送结果
     */
    private Integer callbackType;

    /**
     * log加密电话
     */
    private String cell;

    /**
     * 上传表中用户为一编号 md5手机号
     */
    private String custNum;

    /**
     * API代码
     */
    private String apiCode;

    /**
     * 推送状态，0-未推送，1-推送成功，2-推送失败
     */
    private Integer pushStatus;

    /**
     * 推送状态 0-待推送 1-成功 2-异常
     */
    private Integer status;

    /**
     * 创建日期
     */
    private Integer createDate;

    /**
     * 1-代表TRUE，0-代表FALSE
     */
    private Boolean result;

    /**
     * 返回错误码
     */
    private String errorCode;

    /**
     * 返回错误信息
     */
    private String errorMessage;

    /**
     * 编码
     */
    private String scas;

    /**
     * 媒体名称
     */
    private String mediaName;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 通话状态
     */
    private Integer isConnect;

    /**
     * 短信状态
     */
    private Integer smsSendStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

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

    public Integer getCallbackType() {
        return callbackType;
    }

    public void setCallbackType(Integer callbackType) {
        this.callbackType = callbackType;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum == null ? null : custNum.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
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

    public Integer getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Integer createDate) {
        this.createDate = createDate;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
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

    public String getScas() {
        return scas;
    }

    public void setScas(String scas) {
        this.scas = scas == null ? null : scas.trim();
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName == null ? null : mediaName.trim();
    }

    public String getDataMessage() {
        return dataMessage;
    }

    public void setDataMessage(String dataMessage) {
        this.dataMessage = dataMessage == null ? null : dataMessage.trim();
    }

    public Integer getIsConnect() {
        return isConnect;
    }

    public void setIsConnect(Integer isConnect) {
        this.isConnect = isConnect;
    }

    public Integer getSmsSendStatus() {
        return smsSendStatus;
    }

    public void setSmsSendStatus(Integer smsSendStatus) {
        this.smsSendStatus = smsSendStatus;
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

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}