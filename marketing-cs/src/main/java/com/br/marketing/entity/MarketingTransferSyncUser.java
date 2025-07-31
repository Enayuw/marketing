package com.br.marketing.entity;

import java.util.Date;

public class MarketingTransferSyncUser {
    /**
     * 
     */
    private Long id;

    /**
     * cid
     */
    private String cid;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 请求批次号
     */
    private String requestId;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 客户案件编号
     */
    private String custNum;

    /**
     * 数据源
     */
    private String source;

    /**
     * 机构运营场景
     */
    private String userType;

    /**
     * 转化节点
     */
    private String type;

    /**
     * 客群名称
     */
    private String customName;

    /**
     * 是否注册 1是0否
     */
    private String ifRegister;

    /**
     * 注册时间 yyyy-mm-dd hh:mm:ss:SSS
     */
    private String registerTime;

    /**
     * 是否登录 1是0否
     */
    private String ifLogin;

    /**
     * 登录时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String loginTime;

    /**
     * 是否进件 1是0否
     */
    private String ifApply;

    /**
     * 进件时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String applyDt;

    /**
     * 审批时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String applyTime;

    /**
     * 审批结果 1是0否
     */
    private String applyResult;

    /**
     * 拒绝时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String refuseTime;

    /**
     * 授信时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String auditTime;

    /**
     * 授信总金额
     */
    private String auditAmount;

    /**
     * 是否提现 1是0否
     */
    private String ifLent;

    /**
     * 提现时间yyyy-mm-dd hh:mm:ss:SSS
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
     * 是否结清 1是0否
     */
    private String ifSettle;

    /**
     * 结清时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String settleTime;

    /**
     * 活动类型
     */
    private String activity;

    /**
     * 结案状态 0未结案1已结案
     */
    private String caseStatus;

    /**
     * 案件有效性 0无效1有效
     */
    private String caseEffective;

    /**
     * 是否转化 1是0否
     */
    private String ifTransform;

    /**
     * 转化时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String transformTime;

    /**
     * 创建时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String insertTime;

    /**
     * 业务保留字段1
     */
    private String reserveField1;

    /**
     * 业务保留字段2
     */
    private String reserveField2;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 客户请求日期
     */
    private String requestData;

    /**
     * 客户请求时间
     */
    private String requestTime;

    /**
     * 分表cid
     */
    private String tCid;

    /**
     * 渠道 1永辉金融  2永辉生活
     * @return
     */
    private String channel;

    /**
     * 数据指纹，数据唯一标识
     */
    private Long fingerprint;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName == null ? null : orgName.trim();
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum == null ? null : custNum.trim();
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

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName == null ? null : customName.trim();
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

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime == null ? null : insertTime.trim();
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1 == null ? null : reserveField1.trim();
    }

    public String getReserveField2() {
        return reserveField2;
    }

    public void setReserveField2(String reserveField2) {
        this.reserveField2 = reserveField2 == null ? null : reserveField2.trim();
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

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData == null ? null : requestData.trim();
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime == null ? null : requestTime.trim();
    }

    public String gettCid() {
        return tCid;
    }

    public void settCid(String tCid) {
        this.tCid = tCid == null ? null : tCid.trim();
    }

    public Long getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(Long fingerprint) {
        this.fingerprint = fingerprint;
    }
}