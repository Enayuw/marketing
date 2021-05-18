package com.br.marketing.entity;

import java.util.Date;

public class MarketingDirtyUser {
    /**
     * 
     */
    private Integer id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 客户编号
     */
    private String cusNum;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 姓名
     */
    private String name;

    /**
     * 电话
     */
    private String cell;

    /**
     * 联系人电话，支持加密
     */
    private String linkmanCell;

    /**
     * 家庭地址
     */
    private String homeAddr;

    /**
     * 家庭座机号
     */
    private String telHome;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 时间范围。取值：0-5，单位：年；填入相应数字表示查询最近几年的特殊名单服务，未填或填入其它数字表示默认查询所有时间段的标签。
     */
    private String timeRange;

    /**
     * 
     */
    private String passDate;

    /**
     * 贷前审批结果
     */
    private String approvalResult;

    /**
     * 
     */
    private String loanmaturityDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public String getCusNum() {
        return cusNum;
    }

    public void setCusNum(String cusNum) {
        this.cusNum = cusNum == null ? null : cusNum.trim();
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard == null ? null : idCard.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getLinkmanCell() {
        return linkmanCell;
    }

    public void setLinkmanCell(String linkmanCell) {
        this.linkmanCell = linkmanCell == null ? null : linkmanCell.trim();
    }

    public String getHomeAddr() {
        return homeAddr;
    }

    public void setHomeAddr(String homeAddr) {
        this.homeAddr = homeAddr == null ? null : homeAddr.trim();
    }

    public String getTelHome() {
        return telHome;
    }

    public void setTelHome(String telHome) {
        this.telHome = telHome == null ? null : telHome.trim();
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail == null ? null : mail.trim();
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

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange == null ? null : timeRange.trim();
    }

    public String getPassDate() {
        return passDate;
    }

    public void setPassDate(String passDate) {
        this.passDate = passDate == null ? null : passDate.trim();
    }

    public String getApprovalResult() {
        return approvalResult;
    }

    public void setApprovalResult(String approvalResult) {
        this.approvalResult = approvalResult == null ? null : approvalResult.trim();
    }

    public String getLoanmaturityDate() {
        return loanmaturityDate;
    }

    public void setLoanmaturityDate(String loanmaturityDate) {
        this.loanmaturityDate = loanmaturityDate == null ? null : loanmaturityDate.trim();
    }
}