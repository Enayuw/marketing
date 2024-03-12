package com.br.marketing.entity;

import com.br.marketing.rule.InterfaceParams;

import java.util.Date;

public class DidiCallRecord extends InterfaceParams {
    /**
     *
     */
    private Long id;

    /**
     * log加密电话
     */
    private String cell;

    /**
     * 上传表中用户为一编号 md5手机号
     */
    private String custNum;

    /**
     *
     */
    private String apiCode;

    /**
     * 推送状态 0 待推送 1 成功  2 异常
     */
    private Integer status;

    /**
     * 创建日期
     */
    private Integer createDate;

    /**
     * 1 代表 TRUE，0 代表 FALSE
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
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 系统逻辑记录，失效 或者重复
     */
    private String sysMessage;

    /**
     * 编码
     */
    private String scas;

    /**
     * 媒体名称
     */
    private String mediaName;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 数据描述
     */
    private String dataMessage;

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

    public String getSysMessage() {
        return sysMessage;
    }

    public void setSysMessage(String sysMessage) {
        this.sysMessage = sysMessage == null ? null : sysMessage.trim();
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

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getDataMessage() {
        return dataMessage;
    }

    public void setDataMessage(String dataMessage) {
        this.dataMessage = dataMessage == null ? null : dataMessage.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}