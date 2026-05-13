package com.br.marketing.vo;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MarketingSyncUserVO {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private String apiCode;
    private String cusBatch;
    private String requestBatch;
    private String custNum;
    private String idCard;
    private String name;
    private String cell;
    private String cellMd5;
    private String cellSha256;
    private String cellOriginal;
    private String idCardOriginal;
    private String nameOriginal;
    private String groupType;
    private String userType;
    private String operateType;
    private String registerDate;
    private String registerTime;
    private String reserveField1;
    private String reserveField2;
    private String createTime;
    private String updateTime;
    private String appletDate;
    private Integer status;
    private String failType;
    private String appletTime;
    private Integer isTask;
    private String taskTime;
    private Integer isRepeat;
    private Long id;
    private String cid;
    private String requestId;
    private String orgName;
    private String source;
    private String type;
    private String customName;
    private String ifRegister;
    private String ifLogin;
    private String loginTime;
    private String ifApply;
    private String applyDt;
    private String applyTime;
    private String applyResult;
    private String refuseTime;
    private String auditTime;
    private String auditAmount;
    private String ifLent;
    private String lentTime;
    private String lentAmount;
    private String unlentAmount;
    private String ifSettle;
    private String settleTime;
    private String activity;
    private String caseStatus;
    private String caseEffective;
    private String ifTransform;
    private String transformTime;
    private String insertTime;
    private String requestData;
    private String requestTime;
    private String tCid;
    private String channel;
    private Long fingerprint;

    public static MarketingSyncUserVO fromEntity(MarketingSyncUser user) {
        if (user == null) {
            return null;
        }
        MarketingSyncUserVO vo = new MarketingSyncUserVO();
        vo.setId(user.getId());
        vo.setApiCode(user.getApiCode());
        vo.setCusBatch(user.getCusBatch());
        vo.setRequestBatch(user.getRequestBatch());
        vo.setCustNum(user.getCustNum());
        vo.setIdCard(user.getIdCard());
        vo.setName(user.getName());
        vo.setCell(user.getCell());
        vo.setCellMd5(user.getCellMd5());
        vo.setCellSha256(user.getCellSha256());
        vo.setCellOriginal(user.getCellOriginal());
        vo.setIdCardOriginal(user.getIdCardOriginal());
        vo.setNameOriginal(user.getNameOriginal());
        vo.setGroupType(user.getGroupType());
        vo.setUserType(user.getUserType());
        vo.setOperateType(user.getOperateType());
        vo.setRegisterDate(user.getRegisterDate());
        vo.setReserveField1(user.getReserveField1());
        vo.setReserveField2(user.getReserveField2());
        vo.setCreateTime(formatDate(user.getCreateTime()));
        vo.setUpdateTime(formatDate(user.getUpdateTime()));
        vo.setAppletDate(user.getAppletDate());
        vo.setStatus(user.getStatus());
        vo.setFailType(user.getFailType());
        vo.setAppletTime(formatDate(user.getAppletTime()));
        vo.setIsTask(user.getIsTask());
        vo.setTaskTime(formatDate(user.getTaskTime()));
        vo.setIsRepeat(user.getIsRepeat());
        vo.setFingerprint(user.getFingerprint());
        return vo;
    }

    public static MarketingSyncUserVO fromTransferEntity(MarketingTransferSyncUser user) {
        if (user == null) {
            return null;
        }
        MarketingSyncUserVO vo = new MarketingSyncUserVO();
        vo.setId(user.getId());
        vo.setCid(user.getCid());
        vo.setApiCode(user.getApiCode());
        vo.setRequestId(user.getRequestId());
        vo.setRequestBatch(user.getRequestId());
        vo.setOrgName(user.getOrgName());
        vo.setCustNum(user.getCustNum());
        vo.setSource(user.getSource());
        vo.setUserType(user.getUserType());
        vo.setType(user.getType());
        vo.setCustomName(user.getCustomName());
        vo.setIfRegister(user.getIfRegister());
        vo.setRegisterTime(user.getRegisterTime());
        vo.setRegisterDate(user.getRegisterTime());
        vo.setIfLogin(user.getIfLogin());
        vo.setLoginTime(user.getLoginTime());
        vo.setIfApply(user.getIfApply());
        vo.setApplyDt(user.getApplyDt());
        vo.setApplyTime(user.getApplyTime());
        vo.setApplyResult(user.getApplyResult());
        vo.setRefuseTime(user.getRefuseTime());
        vo.setAuditTime(user.getAuditTime());
        vo.setAuditAmount(user.getAuditAmount());
        vo.setIfLent(user.getIfLent());
        vo.setLentTime(user.getLentTime());
        vo.setLentAmount(user.getLentAmount());
        vo.setUnlentAmount(user.getUnlentAmount());
        vo.setIfSettle(user.getIfSettle());
        vo.setSettleTime(user.getSettleTime());
        vo.setActivity(user.getActivity());
        vo.setCaseStatus(user.getCaseStatus());
        vo.setCaseEffective(user.getCaseEffective());
        vo.setIfTransform(user.getIfTransform());
        vo.setTransformTime(user.getTransformTime());
        vo.setInsertTime(user.getInsertTime());
        vo.setReserveField1(user.getReserveField1());
        vo.setReserveField2(user.getReserveField2());
        vo.setCreateTime(formatDate(user.getCreateTime()));
        vo.setUpdateTime(formatDate(user.getUpdateTime()));
        vo.setRequestData(user.getRequestData());
        vo.setRequestTime(user.getRequestTime());
        vo.settCid(user.gettCid());
        vo.setChannel(user.getChannel());
        vo.setFingerprint(user.getFingerprint());
        return vo;
    }

    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getCusBatch() {
        return cusBatch;
    }

    public void setCusBatch(String cusBatch) {
        this.cusBatch = cusBatch;
    }

    public String getRequestBatch() {
        return requestBatch;
    }

    public void setRequestBatch(String requestBatch) {
        this.requestBatch = requestBatch;
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getCellMd5() {
        return cellMd5;
    }

    public void setCellMd5(String cellMd5) {
        this.cellMd5 = cellMd5;
    }

    public String getCellSha256() {
        return cellSha256;
    }

    public void setCellSha256(String cellSha256) {
        this.cellSha256 = cellSha256;
    }

    public String getCellOriginal() {
        return cellOriginal;
    }

    public void setCellOriginal(String cellOriginal) {
        this.cellOriginal = cellOriginal;
    }

    public String getIdCardOriginal() {
        return idCardOriginal;
    }

    public void setIdCardOriginal(String idCardOriginal) {
        this.idCardOriginal = idCardOriginal;
    }

    public String getNameOriginal() {
        return nameOriginal;
    }

    public void setNameOriginal(String nameOriginal) {
        this.nameOriginal = nameOriginal;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public String getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(String registerTime) {
        this.registerTime = registerTime;
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1;
    }

    public String getReserveField2() {
        return reserveField2;
    }

    public void setReserveField2(String reserveField2) {
        this.reserveField2 = reserveField2;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFailType() {
        return failType;
    }

    public void setFailType(String failType) {
        this.failType = failType;
    }

    public String getAppletTime() {
        return appletTime;
    }

    public void setAppletTime(String appletTime) {
        this.appletTime = appletTime;
    }

    public Integer getIsTask() {
        return isTask;
    }

    public void setIsTask(Integer isTask) {
        this.isTask = isTask;
    }

    public String getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(String taskTime) {
        this.taskTime = taskTime;
    }

    public Integer getIsRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(Integer isRepeat) {
        this.isRepeat = isRepeat;
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
        this.cid = cid;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getIfRegister() {
        return ifRegister;
    }

    public void setIfRegister(String ifRegister) {
        this.ifRegister = ifRegister;
    }

    public String getIfLogin() {
        return ifLogin;
    }

    public void setIfLogin(String ifLogin) {
        this.ifLogin = ifLogin;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getIfApply() {
        return ifApply;
    }

    public void setIfApply(String ifApply) {
        this.ifApply = ifApply;
    }

    public String getApplyDt() {
        return applyDt;
    }

    public void setApplyDt(String applyDt) {
        this.applyDt = applyDt;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getApplyResult() {
        return applyResult;
    }

    public void setApplyResult(String applyResult) {
        this.applyResult = applyResult;
    }

    public String getRefuseTime() {
        return refuseTime;
    }

    public void setRefuseTime(String refuseTime) {
        this.refuseTime = refuseTime;
    }

    public String getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(String auditTime) {
        this.auditTime = auditTime;
    }

    public String getAuditAmount() {
        return auditAmount;
    }

    public void setAuditAmount(String auditAmount) {
        this.auditAmount = auditAmount;
    }

    public String getIfLent() {
        return ifLent;
    }

    public void setIfLent(String ifLent) {
        this.ifLent = ifLent;
    }

    public String getLentTime() {
        return lentTime;
    }

    public void setLentTime(String lentTime) {
        this.lentTime = lentTime;
    }

    public String getLentAmount() {
        return lentAmount;
    }

    public void setLentAmount(String lentAmount) {
        this.lentAmount = lentAmount;
    }

    public String getUnlentAmount() {
        return unlentAmount;
    }

    public void setUnlentAmount(String unlentAmount) {
        this.unlentAmount = unlentAmount;
    }

    public String getIfSettle() {
        return ifSettle;
    }

    public void setIfSettle(String ifSettle) {
        this.ifSettle = ifSettle;
    }

    public String getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(String settleTime) {
        this.settleTime = settleTime;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public String getCaseEffective() {
        return caseEffective;
    }

    public void setCaseEffective(String caseEffective) {
        this.caseEffective = caseEffective;
    }

    public String getIfTransform() {
        return ifTransform;
    }

    public void setIfTransform(String ifTransform) {
        this.ifTransform = ifTransform;
    }

    public String getTransformTime() {
        return transformTime;
    }

    public void setTransformTime(String transformTime) {
        this.transformTime = transformTime;
    }

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String gettCid() {
        return tCid;
    }

    public void settCid(String tCid) {
        this.tCid = tCid;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Long getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(Long fingerprint) {
        this.fingerprint = fingerprint;
    }

}
