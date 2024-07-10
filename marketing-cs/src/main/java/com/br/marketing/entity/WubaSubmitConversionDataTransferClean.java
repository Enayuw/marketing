package com.br.marketing.entity;

import java.util.Date;

public class WubaSubmitConversionDataTransferClean {
    /**
     * 
     */
    private Long id;

    /**
     * md5手机号
     */
    private String cell;

    /**
     * 最近一次撞库时间
     */
    private Date pushTime;

    /**
     * 清洗状态 0-待清洗 1-已清洗
     */
    private Integer cleanStatus;

    /**
     * 最后登录时间
     */
    private String lastlogintime;

    /**
     * 授信申请时间
     */
    private String financeapplytime;

    /**
     * 金融授信状态：0 失败 1 成功
     */
    private String financecreditstatus;

    /**
     * 授信完成时间
     */
    private String financecreditfinishtime;

    /**
     * 提现申请时间
     */
    private String debttime;

    /**
     * 提现通过时间
     */
    private String debtpasstime;

    /**
     * 提现金额
     */
    private String loanamt;

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

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public Integer getCleanStatus() {
        return cleanStatus;
    }

    public void setCleanStatus(Integer cleanStatus) {
        this.cleanStatus = cleanStatus;
    }

    public String getLastlogintime() {
        return lastlogintime;
    }

    public void setLastlogintime(String lastlogintime) {
        this.lastlogintime = lastlogintime == null ? null : lastlogintime.trim();
    }

    public String getFinanceapplytime() {
        return financeapplytime;
    }

    public void setFinanceapplytime(String financeapplytime) {
        this.financeapplytime = financeapplytime == null ? null : financeapplytime.trim();
    }

    public String getFinancecreditstatus() {
        return financecreditstatus;
    }

    public void setFinancecreditstatus(String financecreditstatus) {
        this.financecreditstatus = financecreditstatus == null ? null : financecreditstatus.trim();
    }

    public String getFinancecreditfinishtime() {
        return financecreditfinishtime;
    }

    public void setFinancecreditfinishtime(String financecreditfinishtime) {
        this.financecreditfinishtime = financecreditfinishtime == null ? null : financecreditfinishtime.trim();
    }

    public String getDebttime() {
        return debttime;
    }

    public void setDebttime(String debttime) {
        this.debttime = debttime == null ? null : debttime.trim();
    }

    public String getDebtpasstime() {
        return debtpasstime;
    }

    public void setDebtpasstime(String debtpasstime) {
        this.debtpasstime = debtpasstime == null ? null : debtpasstime.trim();
    }

    public String getLoanamt() {
        return loanamt;
    }

    public void setLoanamt(String loanamt) {
        this.loanamt = loanamt == null ? null : loanamt.trim();
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