package com.br.marketing.entity;

import java.util.Date;

public class SmsCallback {
    /**
     *
     */
    private Long id;

    /**
     * 公司ID
     */
    private String cid;

    /**
     * api接入唯一标识
     */
    private String apiCode;

    /**
     * 回调类型 2-挂机短信；5-短信
     */
    private Integer callBackType;

    /**
     * 场景
     */
    private String userType;

    /**
     * 短信流水号
     */
    private String thirdCallNo;

    /**
     * 短信发送状态：0-失败，1-成功
     */
    private Integer smsSendStatus;

    /**
     * 案件编号
     */
    private String caseNum;

    /**
     * 预留字段1
     */
    private String reserveField1;

    /**
     * 入库日期
     */
    private String createDate;

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

    public Integer getCallBackType() {
        return callBackType;
    }

    public void setCallBackType(Integer callBackType) {
        this.callBackType = callBackType;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getThirdCallNo() {
        return thirdCallNo;
    }

    public void setThirdCallNo(String thirdCallNo) {
        this.thirdCallNo = thirdCallNo == null ? null : thirdCallNo.trim();
    }

    public Integer getSmsSendStatus() {
        return smsSendStatus;
    }

    public void setSmsSendStatus(Integer smsSendStatus) {
        this.smsSendStatus = smsSendStatus;
    }

    public String getCaseNum() {
        return caseNum;
    }

    public void setCaseNum(String caseNum) {
        this.caseNum = caseNum == null ? null : caseNum.trim();
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1 == null ? null : reserveField1.trim();
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
}