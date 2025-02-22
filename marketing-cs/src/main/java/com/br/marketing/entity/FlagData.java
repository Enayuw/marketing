package com.br.marketing.entity;

import java.util.Date;

public class FlagData {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 文件落库状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 清洗状态 清洗状态 0-待清洗 1-清洗中，2-已完成
     */
    private Integer cleanStatus;

    /**
     * 清洗任务id
     */
    private Long taskId;

    /**
     * 案件编号 
     */
    private String custNum;

    /**
     * 场景
     */
    private String userType;

    /**
     * 上传日期
     */
    private String appletDate;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * md5 手机号
     */
    private String cellMd5;

    /**
     * sha256手机号
     */
    private String cellSha256;

    /**
     * log手机号
     */
    private String cellLog;

    /**
     * cell 加解密计算
     */
    private Integer flagCellDecodeComputation;

    /**
     * 打标日期日期
     */
    private Date dtWhitelist;

    /**
     * 是否交集,1-是，0-否
     */
    private Integer flagNewCust;

    /**
     * 状态 0-交集标记计算中；1-交集标记计算完成；null 初始状态
     */
    private Integer flagNewCustComputation;

    /**
     * 客群标记
     */
    private String flagRiskgroup;

    /**
     * 利率标签
     */
    private Integer flagInterest;

    /**
     * 状态 0-客群标记计算中；1-客群标记计算完成；null 初始状态
     */
    private Integer flagCustomerBaseComputation;

    /**
     * 年龄
     */
    private Integer flagAge;

    /**
     * 省份
     */
    private Integer flagProvince;

    /**
     * 特殊名单标签
     */
    private Integer flagSpecialSmall;

    /**
     * 特别风险等级标签  
     */
    private Integer flagSpecialrisklevelRule;

    /**
     * 客群敏感度标签  
     */
    private Integer flagIndexcs;

    /**
     * 多头标签
     */
    private Integer flagApplyloan;

    /**
     * scoreysbase>820
     */
    private Integer flagScoreysbase;

    /**
     * scorefxsbbaseb < 48 并且 scorefxsbbaseb > 0时
     */
    private Integer flagScorefxsbbaseb;

    /**
     * scorescashonregisternologin  >= 460时，
     */
    private Integer flagScorescashonregisternologin;

    /**
     * scorescashonyxxy  >= 60
     */
    private Integer flagScorescashonyxxy;

    /**
     * scorencashonzawswyyym  > 45
     */
    private Integer flagScorencashonzawswyyym;

    /**
     * 状态 0-高风险标记计算中；1-高风险标记计算完成；null 初始状态
     */
    private Integer flagHighRiskComputation;

    /**
     * 外呼黑名单-公共黑名单
     */
    private Integer flagIntellaudioBlacklist;

    /**
     * 交互意愿标签-接挂、提示音挂机
     */
    private Integer flagWithoutWillingness;

    /**
     * 状态 0-黑名单标记计算中；1-黑名单标记计算完成；null 初始状态
     */
    private Integer flagBlacklistComputation;

    /**
     * 跑分是否满足白名单,1-是，0-否
     */
    private Integer flagScoreWhitelist;

    /**
     * 是否白名单,1-是，0-否
     */
    private Integer flagWhitelist;

    /**
     * 状态 0-白名单标记计算中；1-白名单标记计算完成；null 初始状态
     */
    private Integer flagWhitelistComputation;

    /**
     * es状态 0-同步中；1-同步完成；null 初始状态
     */
    private Integer esSyncStatus;

    /**
     * 扩展字段
     */
    private Integer flagExtend1;

    /**
     * 扩展计算字段
     */
    private Integer flagComputationExtend1;

    /**
     * 扩展字段
     */
    private Integer flagExtend2;

    /**
     * 扩展计算字段
     */
    private Integer flagComputationExtend2;

    /**
     * 扩展字段
     */
    private Integer flagExtend3;

    /**
     * 扩展计算字段
     */
    private Integer flagComputationExtend3;

    /**
     * 扩展字段
     */
    private Integer flagExtend4;

    /**
     * 扩展计算字段
     */
    private Integer flagComputationExtend4;

    /**
     * 扩展字段
     */
    private Integer flagExtend5;

    /**
     * 扩展计算字段
     */
    private Integer flagComputationExtend5;

    /**
     * 扩展字段
     */
    private Integer flagExtend6;

    /**
     * 扩展计算字段
     */
    private Integer flagComputationExtend6;

    /**
     * 文件落库数据描述
     */
    private String dataMessage;

    /**
     * 数据描述
     */
    private String dataFlagMessage;

    /**
     * 状态 0-正常1-删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 日期
     */
    private Integer createDate;

    /**
     * 修改时间
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

    public Integer getCleanStatus() {
        return cleanStatus;
    }

    public void setCleanStatus(Integer cleanStatus) {
        this.cleanStatus = cleanStatus;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum == null ? null : custNum.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getCellMd5() {
        return cellMd5;
    }

    public void setCellMd5(String cellMd5) {
        this.cellMd5 = cellMd5 == null ? null : cellMd5.trim();
    }

    public String getCellSha256() {
        return cellSha256;
    }

    public void setCellSha256(String cellSha256) {
        this.cellSha256 = cellSha256 == null ? null : cellSha256.trim();
    }

    public String getCellLog() {
        return cellLog;
    }

    public void setCellLog(String cellLog) {
        this.cellLog = cellLog == null ? null : cellLog.trim();
    }

    public Integer getFlagCellDecodeComputation() {
        return flagCellDecodeComputation;
    }

    public void setFlagCellDecodeComputation(Integer flagCellDecodeComputation) {
        this.flagCellDecodeComputation = flagCellDecodeComputation;
    }

    public Date getDtWhitelist() {
        return dtWhitelist;
    }

    public void setDtWhitelist(Date dtWhitelist) {
        this.dtWhitelist = dtWhitelist;
    }

    public Integer getFlagNewCust() {
        return flagNewCust;
    }

    public void setFlagNewCust(Integer flagNewCust) {
        this.flagNewCust = flagNewCust;
    }

    public Integer getFlagNewCustComputation() {
        return flagNewCustComputation;
    }

    public void setFlagNewCustComputation(Integer flagNewCustComputation) {
        this.flagNewCustComputation = flagNewCustComputation;
    }

    public String getFlagRiskgroup() {
        return flagRiskgroup;
    }

    public void setFlagRiskgroup(String flagRiskgroup) {
        this.flagRiskgroup = flagRiskgroup == null ? null : flagRiskgroup.trim();
    }

    public Integer getFlagInterest() {
        return flagInterest;
    }

    public void setFlagInterest(Integer flagInterest) {
        this.flagInterest = flagInterest;
    }

    public Integer getFlagCustomerBaseComputation() {
        return flagCustomerBaseComputation;
    }

    public void setFlagCustomerBaseComputation(Integer flagCustomerBaseComputation) {
        this.flagCustomerBaseComputation = flagCustomerBaseComputation;
    }

    public Integer getFlagAge() {
        return flagAge;
    }

    public void setFlagAge(Integer flagAge) {
        this.flagAge = flagAge;
    }

    public Integer getFlagProvince() {
        return flagProvince;
    }

    public void setFlagProvince(Integer flagProvince) {
        this.flagProvince = flagProvince;
    }

    public Integer getFlagSpecialSmall() {
        return flagSpecialSmall;
    }

    public void setFlagSpecialSmall(Integer flagSpecialSmall) {
        this.flagSpecialSmall = flagSpecialSmall;
    }

    public Integer getFlagSpecialrisklevelRule() {
        return flagSpecialrisklevelRule;
    }

    public void setFlagSpecialrisklevelRule(Integer flagSpecialrisklevelRule) {
        this.flagSpecialrisklevelRule = flagSpecialrisklevelRule;
    }

    public Integer getFlagIndexcs() {
        return flagIndexcs;
    }

    public void setFlagIndexcs(Integer flagIndexcs) {
        this.flagIndexcs = flagIndexcs;
    }

    public Integer getFlagApplyloan() {
        return flagApplyloan;
    }

    public void setFlagApplyloan(Integer flagApplyloan) {
        this.flagApplyloan = flagApplyloan;
    }

    public Integer getFlagScoreysbase() {
        return flagScoreysbase;
    }

    public void setFlagScoreysbase(Integer flagScoreysbase) {
        this.flagScoreysbase = flagScoreysbase;
    }

    public Integer getFlagScorefxsbbaseb() {
        return flagScorefxsbbaseb;
    }

    public void setFlagScorefxsbbaseb(Integer flagScorefxsbbaseb) {
        this.flagScorefxsbbaseb = flagScorefxsbbaseb;
    }

    public Integer getFlagScorescashonregisternologin() {
        return flagScorescashonregisternologin;
    }

    public void setFlagScorescashonregisternologin(Integer flagScorescashonregisternologin) {
        this.flagScorescashonregisternologin = flagScorescashonregisternologin;
    }

    public Integer getFlagScorescashonyxxy() {
        return flagScorescashonyxxy;
    }

    public void setFlagScorescashonyxxy(Integer flagScorescashonyxxy) {
        this.flagScorescashonyxxy = flagScorescashonyxxy;
    }

    public Integer getFlagScorencashonzawswyyym() {
        return flagScorencashonzawswyyym;
    }

    public void setFlagScorencashonzawswyyym(Integer flagScorencashonzawswyyym) {
        this.flagScorencashonzawswyyym = flagScorencashonzawswyyym;
    }

    public Integer getFlagHighRiskComputation() {
        return flagHighRiskComputation;
    }

    public void setFlagHighRiskComputation(Integer flagHighRiskComputation) {
        this.flagHighRiskComputation = flagHighRiskComputation;
    }

    public Integer getFlagIntellaudioBlacklist() {
        return flagIntellaudioBlacklist;
    }

    public void setFlagIntellaudioBlacklist(Integer flagIntellaudioBlacklist) {
        this.flagIntellaudioBlacklist = flagIntellaudioBlacklist;
    }

    public Integer getFlagWithoutWillingness() {
        return flagWithoutWillingness;
    }

    public void setFlagWithoutWillingness(Integer flagWithoutWillingness) {
        this.flagWithoutWillingness = flagWithoutWillingness;
    }

    public Integer getFlagBlacklistComputation() {
        return flagBlacklistComputation;
    }

    public void setFlagBlacklistComputation(Integer flagBlacklistComputation) {
        this.flagBlacklistComputation = flagBlacklistComputation;
    }

    public Integer getFlagScoreWhitelist() {
        return flagScoreWhitelist;
    }

    public void setFlagScoreWhitelist(Integer flagScoreWhitelist) {
        this.flagScoreWhitelist = flagScoreWhitelist;
    }

    public Integer getFlagWhitelist() {
        return flagWhitelist;
    }

    public void setFlagWhitelist(Integer flagWhitelist) {
        this.flagWhitelist = flagWhitelist;
    }

    public Integer getFlagWhitelistComputation() {
        return flagWhitelistComputation;
    }

    public void setFlagWhitelistComputation(Integer flagWhitelistComputation) {
        this.flagWhitelistComputation = flagWhitelistComputation;
    }

    public Integer getEsSyncStatus() {
        return esSyncStatus;
    }

    public void setEsSyncStatus(Integer esSyncStatus) {
        this.esSyncStatus = esSyncStatus;
    }

    public Integer getFlagExtend1() {
        return flagExtend1;
    }

    public void setFlagExtend1(Integer flagExtend1) {
        this.flagExtend1 = flagExtend1;
    }

    public Integer getFlagComputationExtend1() {
        return flagComputationExtend1;
    }

    public void setFlagComputationExtend1(Integer flagComputationExtend1) {
        this.flagComputationExtend1 = flagComputationExtend1;
    }

    public Integer getFlagExtend2() {
        return flagExtend2;
    }

    public void setFlagExtend2(Integer flagExtend2) {
        this.flagExtend2 = flagExtend2;
    }

    public Integer getFlagComputationExtend2() {
        return flagComputationExtend2;
    }

    public void setFlagComputationExtend2(Integer flagComputationExtend2) {
        this.flagComputationExtend2 = flagComputationExtend2;
    }

    public Integer getFlagExtend3() {
        return flagExtend3;
    }

    public void setFlagExtend3(Integer flagExtend3) {
        this.flagExtend3 = flagExtend3;
    }

    public Integer getFlagComputationExtend3() {
        return flagComputationExtend3;
    }

    public void setFlagComputationExtend3(Integer flagComputationExtend3) {
        this.flagComputationExtend3 = flagComputationExtend3;
    }

    public Integer getFlagExtend4() {
        return flagExtend4;
    }

    public void setFlagExtend4(Integer flagExtend4) {
        this.flagExtend4 = flagExtend4;
    }

    public Integer getFlagComputationExtend4() {
        return flagComputationExtend4;
    }

    public void setFlagComputationExtend4(Integer flagComputationExtend4) {
        this.flagComputationExtend4 = flagComputationExtend4;
    }

    public Integer getFlagExtend5() {
        return flagExtend5;
    }

    public void setFlagExtend5(Integer flagExtend5) {
        this.flagExtend5 = flagExtend5;
    }

    public Integer getFlagComputationExtend5() {
        return flagComputationExtend5;
    }

    public void setFlagComputationExtend5(Integer flagComputationExtend5) {
        this.flagComputationExtend5 = flagComputationExtend5;
    }

    public Integer getFlagExtend6() {
        return flagExtend6;
    }

    public void setFlagExtend6(Integer flagExtend6) {
        this.flagExtend6 = flagExtend6;
    }

    public Integer getFlagComputationExtend6() {
        return flagComputationExtend6;
    }

    public void setFlagComputationExtend6(Integer flagComputationExtend6) {
        this.flagComputationExtend6 = flagComputationExtend6;
    }

    public String getDataMessage() {
        return dataMessage;
    }

    public void setDataMessage(String dataMessage) {
        this.dataMessage = dataMessage == null ? null : dataMessage.trim();
    }

    public String getDataFlagMessage() {
        return dataFlagMessage;
    }

    public void setDataFlagMessage(String dataFlagMessage) {
        this.dataFlagMessage = dataFlagMessage == null ? null : dataFlagMessage.trim();
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Integer createDate) {
        this.createDate = createDate;
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