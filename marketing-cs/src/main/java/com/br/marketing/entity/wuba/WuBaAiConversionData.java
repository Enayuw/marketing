package com.br.marketing.entity.wuba;

import lombok.Data;
import lombok.Setter;

import java.util.Date;

public class WuBaAiConversionData {
    /**
     * 主键
     */
    private Long id;

    /**
     * 关联b_58ai_fetch_task.id
     */
    private Long taskId;

    /**
     * 数据ID(来自接口)
     */
    private String dataId;

    /**
     * 转化事件时间
     */
    private String dwEventTime;

    /**
     * 加密手机号
     */
    private String mobileEncrypt;

    /**
     * 用户类型/场景
     */
    private String userType;

    /**
     * 最后登录时间
     */
    private String lastLoginTime;

    /**
     * 负债时间
     */
    private String debtTime;

    /**
     * 进件申请时间
     */
    private String debtApplyTime;

    /**
     * 进件通过时间
     */
    private String debtPassTime;

    /**
     * 外呼有效期截止时间(dw_event_time+7天)
     */
    private String expireDate;

    /**
     * 转化状态:0-已转化 1-未转化
     */
    private String inversionStatus;

    /**
     * 推送决策状态:0-未推送 1-已推送
     */
    private String pushDecisionStatus;

    /**
     * 扩展字段
     */
    private String reserveField;

    @Setter
    private Integer cleanStatus;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId == null ? null : dataId.trim();
    }

    public String getDwEventTime() {
        return dwEventTime;
    }

    public void setDwEventTime(String dwEventTime) {
        this.dwEventTime = dwEventTime == null ? null : dwEventTime.trim();
    }

    public String getMobileEncrypt() {
        return mobileEncrypt;
    }

    public void setMobileEncrypt(String mobileEncrypt) {
        this.mobileEncrypt = mobileEncrypt == null ? null : mobileEncrypt.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime == null ? null : lastLoginTime.trim();
    }

    public String getDebtTime() {
        return debtTime;
    }

    public void setDebtTime(String debtTime) {
        this.debtTime = debtTime == null ? null : debtTime.trim();
    }

    public String getDebtApplyTime() {
        return debtApplyTime;
    }

    public void setDebtApplyTime(String debtApplyTime) {
        this.debtApplyTime = debtApplyTime == null ? null : debtApplyTime.trim();
    }

    public String getDebtPassTime() {
        return debtPassTime;
    }

    public void setDebtPassTime(String debtPassTime) {
        this.debtPassTime = debtPassTime == null ? null : debtPassTime.trim();
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate == null ? null : expireDate.trim();
    }

    public String getInversionStatus() {
        return inversionStatus;
    }

    public void setInversionStatus(String inversionStatus) {
        this.inversionStatus = inversionStatus == null ? null : inversionStatus.trim();
    }

    public String getPushDecisionStatus() {
        return pushDecisionStatus;
    }

    public void setPushDecisionStatus(String pushDecisionStatus) {
        this.pushDecisionStatus = pushDecisionStatus == null ? null : pushDecisionStatus.trim();
    }

    public String getReserveField() {
        return reserveField;
    }

    public void setReserveField(String reserveField) {
        this.reserveField = reserveField == null ? null : reserveField.trim();
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