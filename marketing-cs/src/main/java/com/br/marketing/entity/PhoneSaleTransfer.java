package com.br.marketing.entity;

import java.util.Date;

public class PhoneSaleTransfer {
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
    private String localId;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer mStatus;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 用户id
     */
    private String uid;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 手机号aes加密
     */
    private String phoneAes;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 数据源
     */
    private String source;

    /**
     * 机构运营场景
     */
    private String userType;

    /**
     * 机器人转化节点类型
     */
    private String type;

    /**
     * 是否注册
     */
    private String ifRegister;

    /**
     * 注册时间
     */
    private String registerTime;

    /**
     * 是否登录
     */
    private String ifLogin;

    /**
     * 登录时间
     */
    private String loginTime;

    /**
     * 是否进件
     */
    private String ifApply;

    /**
     * 进件时间
     */
    private String applyDt;

    /**
     * 审批时间
     */
    private String applyTime;

    /**
     * 审批结果
     */
    private String applyResult;

    /**
     * 拒绝时间
     */
    private String refuseTime;

    /**
     * 授信时间
     */
    private String auditTime;

    /**
     * 授信总金额
     */
    private String auditAmount;

    /**
     * 是否提现
     */
    private String ifLent;

    /**
     * 提现时间
     */
    private String lentTime;

    /**
     * 提现金额
     */
    private String lentAmount;

    /**
     * 未提现额度
     */
    private String unlentAmount;

    /**
     * 是否结清
     */
    private String ifSettle;

    /**
     * 结清时间
     */
    private String settleTime;

    /**
     * 0-无活动  1-红包  2-24%利率  3-30%利率
     */
    private String activity;

    /**
     * 结案状态：0未结案 1已结案
     */
    private String caseStatus;

    /**
     * 案件有效性：0无效 1有效
     */
    private String caseEffective;

    /**
     * 是否转化：1是0否
     */
    private String ifTransform;

    /**
     * 转化时间
     */
    private String transformTime;

    /**
     * 案件转化状态（当前状态）
     */
    private String status;

    /**
     * 创建时间（严格时序）
     */
    private String insertTime;

    /**
     * 转化规则类型
     */
    private String transformStatus;

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

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId == null ? null : localId.trim();
    }

    public Integer getmStatus() {
        return mStatus;
    }

    public void setmStatus(Integer mStatus) {
        this.mStatus = mStatus;
    }

    public String getDataMessage() {
        return dataMessage;
    }

    public void setDataMessage(String dataMessage) {
        this.dataMessage = dataMessage == null ? null : dataMessage.trim();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getPhoneAes() {
        return phoneAes;
    }

    public void setPhoneAes(String phoneAes) {
        this.phoneAes = phoneAes == null ? null : phoneAes.trim();
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName == null ? null : orgName.trim();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getIfRegister() {
        return ifRegister;
    }

    public void setIfRegister(String ifRegister) {
        this.ifRegister = ifRegister == null ? null : ifRegister.trim();
    }

    public String getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(String registerTime) {
        this.registerTime = registerTime == null ? null : registerTime.trim();
    }

    public String getIfLogin() {
        return ifLogin;
    }

    public void setIfLogin(String ifLogin) {
        this.ifLogin = ifLogin == null ? null : ifLogin.trim();
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime == null ? null : loginTime.trim();
    }

    public String getIfApply() {
        return ifApply;
    }

    public void setIfApply(String ifApply) {
        this.ifApply = ifApply == null ? null : ifApply.trim();
    }

    public String getApplyDt() {
        return applyDt;
    }

    public void setApplyDt(String applyDt) {
        this.applyDt = applyDt == null ? null : applyDt.trim();
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime == null ? null : applyTime.trim();
    }

    public String getApplyResult() {
        return applyResult;
    }

    public void setApplyResult(String applyResult) {
        this.applyResult = applyResult == null ? null : applyResult.trim();
    }

    public String getRefuseTime() {
        return refuseTime;
    }

    public void setRefuseTime(String refuseTime) {
        this.refuseTime = refuseTime == null ? null : refuseTime.trim();
    }

    public String getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(String auditTime) {
        this.auditTime = auditTime == null ? null : auditTime.trim();
    }

    public String getAuditAmount() {
        return auditAmount;
    }

    public void setAuditAmount(String auditAmount) {
        this.auditAmount = auditAmount == null ? null : auditAmount.trim();
    }

    public String getIfLent() {
        return ifLent;
    }

    public void setIfLent(String ifLent) {
        this.ifLent = ifLent == null ? null : ifLent.trim();
    }

    public String getLentTime() {
        return lentTime;
    }

    public void setLentTime(String lentTime) {
        this.lentTime = lentTime == null ? null : lentTime.trim();
    }

    public String getLentAmount() {
        return lentAmount;
    }

    public void setLentAmount(String lentAmount) {
        this.lentAmount = lentAmount == null ? null : lentAmount.trim();
    }

    public String getUnlentAmount() {
        return unlentAmount;
    }

    public void setUnlentAmount(String unlentAmount) {
        this.unlentAmount = unlentAmount == null ? null : unlentAmount.trim();
    }

    public String getIfSettle() {
        return ifSettle;
    }

    public void setIfSettle(String ifSettle) {
        this.ifSettle = ifSettle == null ? null : ifSettle.trim();
    }

    public String getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(String settleTime) {
        this.settleTime = settleTime == null ? null : settleTime.trim();
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity == null ? null : activity.trim();
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus == null ? null : caseStatus.trim();
    }

    public String getCaseEffective() {
        return caseEffective;
    }

    public void setCaseEffective(String caseEffective) {
        this.caseEffective = caseEffective == null ? null : caseEffective.trim();
    }

    public String getIfTransform() {
        return ifTransform;
    }

    public void setIfTransform(String ifTransform) {
        this.ifTransform = ifTransform == null ? null : ifTransform.trim();
    }

    public String getTransformTime() {
        return transformTime;
    }

    public void setTransformTime(String transformTime) {
        this.transformTime = transformTime == null ? null : transformTime.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime == null ? null : insertTime.trim();
    }

    public String getTransformStatus() {
        return transformStatus;
    }

    public void setTransformStatus(String transformStatus) {
        this.transformStatus = transformStatus == null ? null : transformStatus.trim();
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