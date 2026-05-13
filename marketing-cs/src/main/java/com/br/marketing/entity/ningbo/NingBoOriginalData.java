package com.br.marketing.entity.ningbo;

import java.math.BigDecimal;
import java.util.Date;

public class NingBoOriginalData {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联b_ningbo_data_task.id
     */
    private Long taskId;

    /**
     * 数据批次日期(T日)，格式:yyyy-MM-dd
     */
    private Date taskDate;

    /**
     * 银行名，固定值:宁波银行信用卡中心
     */
    private String bankName;

    /**
     * 银行名2，固定值:宁波银行
     */
    private String bankName2;

    /**
     * 中文姓名
     */
    private String firstName;

    /**
     * 性别，固定值:男或女
     */
    private String gender;

    /**
     * 当前欠款
     */
    private BigDecimal curArearAmt;

    /**
     * 账单剩余未还金额
     */
    private BigDecimal billingNoRepayAmt;

    /**
     * 最小还款额未还金额
     */
    private BigDecimal stmMindueNoRepay;

    /**
     * 逾期金额
     */
    private BigDecimal overDueAmt;

    /**
     * 手机号码(序列号/加密字符)
     */
    private String moPhone;

    /**
     * 卡号(后四位)
     */
    private String cardNbr;

    /**
     * 到期还款日，格式:yyyymmdd
     */
    private String pmtDueDate;

    /**
     * 账单日，格式:两位数字(如08)
     */
    private String cycleNbr;

    /**
     * 当前逾期期数
     */
    private Integer mthsOdue;

    /**
     * 逾期天数
     */
    private Integer overDueDay;

    /**
     * 智能外呼期内已拨打天数
     */
    private Integer aiCalledDays;

    /**
     * 智能外呼期内已接通次数
     */
    private Integer aiCallConnTimes;

    /**
     * 用户风险等级:高/中/低
     */
    private String userRiskLevel;

    /**
     * 场景(线下提供)
     */
    private String userType;

    /**
     * 附加字段(用于存储未明确映射的字段)
     */
    private String reserveField1;

    /**
     * 策略编号(线下提供)
     */
    private String strategyCode;

    /**
     * 使用的映射配置api_code，关联b_ningbo_field_config.api_code
     */
    private String apiCode;

    /**
     * 数据创建时间
     */
    private Date createTime;

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

    public Date getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(Date taskDate) {
        this.taskDate = taskDate;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName == null ? null : bankName.trim();
    }

    public String getBankName2() {
        return bankName2;
    }

    public void setBankName2(String bankName2) {
        this.bankName2 = bankName2 == null ? null : bankName2.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? null : firstName.trim();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender == null ? null : gender.trim();
    }

    public BigDecimal getCurArearAmt() {
        return curArearAmt;
    }

    public void setCurArearAmt(BigDecimal curArearAmt) {
        this.curArearAmt = curArearAmt;
    }

    public BigDecimal getBillingNoRepayAmt() {
        return billingNoRepayAmt;
    }

    public void setBillingNoRepayAmt(BigDecimal billingNoRepayAmt) {
        this.billingNoRepayAmt = billingNoRepayAmt;
    }

    public BigDecimal getStmMindueNoRepay() {
        return stmMindueNoRepay;
    }

    public void setStmMindueNoRepay(BigDecimal stmMindueNoRepay) {
        this.stmMindueNoRepay = stmMindueNoRepay;
    }

    public BigDecimal getOverDueAmt() {
        return overDueAmt;
    }

    public void setOverDueAmt(BigDecimal overDueAmt) {
        this.overDueAmt = overDueAmt;
    }

    public String getMoPhone() {
        return moPhone;
    }

    public void setMoPhone(String moPhone) {
        this.moPhone = moPhone == null ? null : moPhone.trim();
    }

    public String getCardNbr() {
        return cardNbr;
    }

    public void setCardNbr(String cardNbr) {
        this.cardNbr = cardNbr == null ? null : cardNbr.trim();
    }

    public String getPmtDueDate() {
        return pmtDueDate;
    }

    public void setPmtDueDate(String pmtDueDate) {
        this.pmtDueDate = pmtDueDate == null ? null : pmtDueDate.trim();
    }

    public String getCycleNbr() {
        return cycleNbr;
    }

    public void setCycleNbr(String cycleNbr) {
        this.cycleNbr = cycleNbr == null ? null : cycleNbr.trim();
    }

    public Integer getMthsOdue() {
        return mthsOdue;
    }

    public void setMthsOdue(Integer mthsOdue) {
        this.mthsOdue = mthsOdue;
    }

    public Integer getOverDueDay() {
        return overDueDay;
    }

    public void setOverDueDay(Integer overDueDay) {
        this.overDueDay = overDueDay;
    }

    public Integer getAiCalledDays() {
        return aiCalledDays;
    }

    public void setAiCalledDays(Integer aiCalledDays) {
        this.aiCalledDays = aiCalledDays;
    }

    public Integer getAiCallConnTimes() {
        return aiCallConnTimes;
    }

    public void setAiCallConnTimes(Integer aiCallConnTimes) {
        this.aiCallConnTimes = aiCallConnTimes;
    }

    public String getUserRiskLevel() {
        return userRiskLevel;
    }

    public void setUserRiskLevel(String userRiskLevel) {
        this.userRiskLevel = userRiskLevel == null ? null : userRiskLevel.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1 == null ? null : reserveField1.trim();
    }

    public String getStrategyCode() {
        return strategyCode;
    }

    public void setStrategyCode(String strategyCode) {
        this.strategyCode = strategyCode == null ? null : strategyCode.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}