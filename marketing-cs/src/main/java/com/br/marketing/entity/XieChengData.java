package com.br.marketing.entity;

import java.util.Date;

public class XieChengData {
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
     * 数据来源id
     */
    private Long originId;

    /**
     * 幂等键
     */
    private String idempotentKey;

    /**
     * 类型
     */
    private String type;

    /**
     * 广告类型：H5，SMS，IVR
     */
    private String actionType;

    /**
     * 广告唯一标识，用户每点击一次生成一个，由合作方生成
     */
    private String clickId;

    /**
     * 加密手机号（与撞库时的加密方法一致）
     */
    private String clickTel;

    /**
     * 状态 1-未推送；2-推送
     */
    private Integer pushStatus;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 扩展字段
     */
    private String extend;

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

    /**
     * 新-加密手机号(与撞库时的加密方法一致)
     */
    private String sha256Tel;

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

    public Long getOriginId() {
        return originId;
    }

    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    public String getIdempotentKey() {
        return idempotentKey;
    }

    public void setIdempotentKey(String idempotentKey) {
        this.idempotentKey = idempotentKey == null ? null : idempotentKey.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType == null ? null : actionType.trim();
    }

    public String getClickId() {
        return clickId;
    }

    public void setClickId(String clickId) {
        this.clickId = clickId == null ? null : clickId.trim();
    }

    public String getClickTel() {
        return clickTel;
    }

    public void setClickTel(String clickTel) {
        this.clickTel = clickTel == null ? null : clickTel.trim();
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

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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

    public String getSha256Tel() {
        return sha256Tel;
    }

    public void setSha256Tel(String sha256Tel) {
        this.sha256Tel = sha256Tel == null ? null : sha256Tel.trim();
    }
}