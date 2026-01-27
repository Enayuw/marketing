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
     * 推送状态 0-正常，1-重复
     */
    private Integer status;

    /**
     * 创建日期
     */
    private String createDate;

    /**
     * 编码
     */
    private String scas;

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

    /**
     * 推送类型，1-通话成功，2-短信成功，3-构造通话成功，4-构造短信成功
     */
    private Integer pushType;

    /**
     * 数据构造时间
     */
    private Date constructTime;

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

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate == null ? null : createDate.trim();
    }

    public String getScas() {
        return scas;
    }

    public void setScas(String scas) {
        this.scas = scas == null ? null : scas.trim();
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

    public Integer getPushType() {
        return pushType;
    }

    public void setPushType(Integer pushType) {
        this.pushType = pushType;
    }

    public Date getConstructTime() {
        return constructTime;
    }

    public void setConstructTime(Date constructTime) {
        this.constructTime = constructTime;
    }
}