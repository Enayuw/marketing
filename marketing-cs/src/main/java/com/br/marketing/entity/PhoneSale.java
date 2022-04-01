package com.br.marketing.entity;

import java.util.Date;

public class PhoneSale {
    /**
     * 
     */
    private Long id;

    /**
     * 合作客户ID
     */
    private String apiCid;

    /**
     * 
     */
    private String apiCode;

    /**
     * 文件上传日志id
     */
    private String syncLogId;

    /**
     * 本地文件记录id
     */
    private String localId;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

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
     * 姓名
     */
    private String name;

    /**
     * 姓名aes加密
     */
    private String nameAes;

    /**
     * 性别
     */
    private String gender;

    /**
     * 营销分
     */
    private String marketscore;

    /**
     * 风控分
     */
    private String riskscore;

    /**
     * 机构名称
     */
    private String orgname;

    /**
     * 数据源
     */
    private String source;

    /**
     * 机构运营场景
     */
    private String userType;

    /**
     * 产品信息
     */
    private String productName;

    /**
     * 乐花卡类型（1 人工结清 2人工未结清）
     */
    private String flagType;

    /**
     * 机器人转化节点类型
     */
    private String type;

    /**
     * 意向等级
     */
    private String level;

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
     * 页面节点
     */
    private String pagenode;

    /**
     * 1人工 2机器人
     */
    private String optype;

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
     * 活动类型
     */
    private String activity;

    /**
     * 推荐产品
     */
    private String production;

    /**
     * 经营地区
     */
    private String region;

    /**
     * 近3天是否申请：1是0否
     */
    private String yxFlag3d;

    /**
     * 近7天是否申请：1是0否
     */
    private String yxFlag7d;

    /**
     * 近15天是否申请：1是0否
     */
    private String yxFlag15d;

    /**
     * 近30天是否申请：1是0否
     */
    private String yxFlag1m;

    /**
     * 是否有房：1是0否
     */
    private String personFlagHouse;

    /**
     * 是否有车：1是0否
     */
    private String personFlagCar;

    /**
     * 是否有寿险：1是0否
     */
    private String personFlagInsur;

    /**
     * 是否命中国网白名单：1是0否
     */
    private String whiteListGw;

    /**
     * 是否命中发票白名单：1是0否
     */
    private String whiteListFp;

    /**
     * 是否命中烟草白名单：1是0否
     */
    private String whiteListYc;

    /**
     * 拨打优先级（枚举值：1、2、3）
     */
    private String prioritysymbol;

    /**
     * 扩展字段
     */
    private String extend;

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

    public String getApiCid() {
        return apiCid;
    }

    public void setApiCid(String apiCid) {
        this.apiCid = apiCid == null ? null : apiCid.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getSyncLogId() {
        return syncLogId;
    }

    public void setSyncLogId(String syncLogId) {
        this.syncLogId = syncLogId == null ? null : syncLogId.trim();
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId == null ? null : localId.trim();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getNameAes() {
        return nameAes;
    }

    public void setNameAes(String nameAes) {
        this.nameAes = nameAes == null ? null : nameAes.trim();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender == null ? null : gender.trim();
    }

    public String getMarketscore() {
        return marketscore;
    }

    public void setMarketscore(String marketscore) {
        this.marketscore = marketscore == null ? null : marketscore.trim();
    }

    public String getRiskscore() {
        return riskscore;
    }

    public void setRiskscore(String riskscore) {
        this.riskscore = riskscore == null ? null : riskscore.trim();
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname == null ? null : orgname.trim();
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName == null ? null : productName.trim();
    }

    public String getFlagType() {
        return flagType;
    }

    public void setFlagType(String flagType) {
        this.flagType = flagType == null ? null : flagType.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level == null ? null : level.trim();
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

    public String getPagenode() {
        return pagenode;
    }

    public void setPagenode(String pagenode) {
        this.pagenode = pagenode == null ? null : pagenode.trim();
    }

    public String getOptype() {
        return optype;
    }

    public void setOptype(String optype) {
        this.optype = optype == null ? null : optype.trim();
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

    public String getProduction() {
        return production;
    }

    public void setProduction(String production) {
        this.production = production == null ? null : production.trim();
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region == null ? null : region.trim();
    }

    public String getYxFlag3d() {
        return yxFlag3d;
    }

    public void setYxFlag3d(String yxFlag3d) {
        this.yxFlag3d = yxFlag3d == null ? null : yxFlag3d.trim();
    }

    public String getYxFlag7d() {
        return yxFlag7d;
    }

    public void setYxFlag7d(String yxFlag7d) {
        this.yxFlag7d = yxFlag7d == null ? null : yxFlag7d.trim();
    }

    public String getYxFlag15d() {
        return yxFlag15d;
    }

    public void setYxFlag15d(String yxFlag15d) {
        this.yxFlag15d = yxFlag15d == null ? null : yxFlag15d.trim();
    }

    public String getYxFlag1m() {
        return yxFlag1m;
    }

    public void setYxFlag1m(String yxFlag1m) {
        this.yxFlag1m = yxFlag1m == null ? null : yxFlag1m.trim();
    }

    public String getPersonFlagHouse() {
        return personFlagHouse;
    }

    public void setPersonFlagHouse(String personFlagHouse) {
        this.personFlagHouse = personFlagHouse == null ? null : personFlagHouse.trim();
    }

    public String getPersonFlagCar() {
        return personFlagCar;
    }

    public void setPersonFlagCar(String personFlagCar) {
        this.personFlagCar = personFlagCar == null ? null : personFlagCar.trim();
    }

    public String getPersonFlagInsur() {
        return personFlagInsur;
    }

    public void setPersonFlagInsur(String personFlagInsur) {
        this.personFlagInsur = personFlagInsur == null ? null : personFlagInsur.trim();
    }

    public String getWhiteListGw() {
        return whiteListGw;
    }

    public void setWhiteListGw(String whiteListGw) {
        this.whiteListGw = whiteListGw == null ? null : whiteListGw.trim();
    }

    public String getWhiteListFp() {
        return whiteListFp;
    }

    public void setWhiteListFp(String whiteListFp) {
        this.whiteListFp = whiteListFp == null ? null : whiteListFp.trim();
    }

    public String getWhiteListYc() {
        return whiteListYc;
    }

    public void setWhiteListYc(String whiteListYc) {
        this.whiteListYc = whiteListYc == null ? null : whiteListYc.trim();
    }

    public String getPrioritysymbol() {
        return prioritysymbol;
    }

    public void setPrioritysymbol(String prioritysymbol) {
        this.prioritysymbol = prioritysymbol == null ? null : prioritysymbol.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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