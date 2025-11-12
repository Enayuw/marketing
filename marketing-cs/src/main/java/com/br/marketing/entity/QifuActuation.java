package com.br.marketing.entity;

import java.util.Date;

public class QifuActuation {
    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 月份
     */
    private String issueMonth;

    /**
     * 下发日期
     */
    private String issueDate;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 供应商
     */
    private String supplier;

    /**
     * 分组类型名称
     */
    private String groupTypeName;

    /**
     * 有效期
     */
    private String validDate;

    /**
     * 授信人数
     */
    private Integer creditUserCount;

    /**
     * app 登录人数
     */
    private Integer appLoginUserCount;

    /**
     * 发起人数
     */
    private Integer startUserCount;

    /**
     * 动支人数_首动支
     */
    private Integer userLoanCount;

    /**
     * app 登录率 
     */
    private String appLoginRate;

    /**
     * 人头发起率
     */
    private String userStartRate;

    /**
     * 人头动支率_首动支
     */
    private String userLoanRate;

    /**
     * 创建日期
     */
    private String createDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

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

    public String getIssueMonth() {
        return issueMonth;
    }

    public void setIssueMonth(String issueMonth) {
        this.issueMonth = issueMonth == null ? null : issueMonth.trim();
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate == null ? null : issueDate.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier == null ? null : supplier.trim();
    }

    public String getGroupTypeName() {
        return groupTypeName;
    }

    public void setGroupTypeName(String groupTypeName) {
        this.groupTypeName = groupTypeName == null ? null : groupTypeName.trim();
    }

    public String getValidDate() {
        return validDate;
    }

    public void setValidDate(String validDate) {
        this.validDate = validDate == null ? null : validDate.trim();
    }

    public Integer getCreditUserCount() {
        return creditUserCount;
    }

    public void setCreditUserCount(Integer creditUserCount) {
        this.creditUserCount = creditUserCount;
    }

    public Integer getAppLoginUserCount() {
        return appLoginUserCount;
    }

    public void setAppLoginUserCount(Integer appLoginUserCount) {
        this.appLoginUserCount = appLoginUserCount;
    }

    public Integer getStartUserCount() {
        return startUserCount;
    }

    public void setStartUserCount(Integer startUserCount) {
        this.startUserCount = startUserCount;
    }

    public Integer getUserLoanCount() {
        return userLoanCount;
    }

    public void setUserLoanCount(Integer userLoanCount) {
        this.userLoanCount = userLoanCount;
    }

    public String getAppLoginRate() {
        return appLoginRate;
    }

    public void setAppLoginRate(String appLoginRate) {
        this.appLoginRate = appLoginRate == null ? null : appLoginRate.trim();
    }

    public String getUserStartRate() {
        return userStartRate;
    }

    public void setUserStartRate(String userStartRate) {
        this.userStartRate = userStartRate == null ? null : userStartRate.trim();
    }

    public String getUserLoanRate() {
        return userLoanRate;
    }

    public void setUserLoanRate(String userLoanRate) {
        this.userLoanRate = userLoanRate == null ? null : userLoanRate.trim();
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate == null ? null : createDate.trim();
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

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }
}