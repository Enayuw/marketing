package com.br.marketing.entity;

import java.util.Date;

public class WubaSubmitConversionDataLog {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 上报表id
     */
    private Long dataId;

    /**
     * md5手机号
     */
    private String cell;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 上报结果， 0-上报中，1-转化成功，2-转化失败
     */
    private Boolean submitResult;

    /**
     * 最后登录时间
     */
    private String lastLoginTime;

    /**
     * 授信申请时间
     */
    private String financeApplyTime;

    /**
     * 金融授信状态：0 失败 1 成功
     */
    private String financeCreditStatus;

    /**
     * 授信完成时间
     */
    private String financeCreditFinishTime;

    /**
     * 提现申请时间
     */
    private String debtTime;

    /**
     * 提现通过时间
     */
    private String debtPassTime;

    /**
     * 提现金额
     */
    private String loanAmt;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 删除标识 0-正常，1-删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
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

    public Long getDataId() {
        return dataId;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public Boolean getSubmitResult() {
        return submitResult;
    }

    public void setSubmitResult(Boolean submitResult) {
        this.submitResult = submitResult;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime == null ? null : lastLoginTime.trim();
    }

    public String getFinanceApplyTime() {
        return financeApplyTime;
    }

    public void setFinanceApplyTime(String financeApplyTime) {
        this.financeApplyTime = financeApplyTime == null ? null : financeApplyTime.trim();
    }

    public String getFinanceCreditStatus() {
        return financeCreditStatus;
    }

    public void setFinanceCreditStatus(String financeCreditStatus) {
        this.financeCreditStatus = financeCreditStatus == null ? null : financeCreditStatus.trim();
    }

    public String getFinanceCreditFinishTime() {
        return financeCreditFinishTime;
    }

    public void setFinanceCreditFinishTime(String financeCreditFinishTime) {
        this.financeCreditFinishTime = financeCreditFinishTime == null ? null : financeCreditFinishTime.trim();
    }

    public String getDebtTime() {
        return debtTime;
    }

    public void setDebtTime(String debtTime) {
        this.debtTime = debtTime == null ? null : debtTime.trim();
    }

    public String getDebtPassTime() {
        return debtPassTime;
    }

    public void setDebtPassTime(String debtPassTime) {
        this.debtPassTime = debtPassTime == null ? null : debtPassTime.trim();
    }

    public String getLoanAmt() {
        return loanAmt;
    }

    public void setLoanAmt(String loanAmt) {
        this.loanAmt = loanAmt == null ? null : loanAmt.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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