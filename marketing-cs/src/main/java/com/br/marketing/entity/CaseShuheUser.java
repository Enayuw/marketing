package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;

public class CaseShuheUser implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 用户唯一编号
     */
    private String custNum;

    /**
     * 电话（营销加密后）
     */
    private String cell;

    /**
     * 场景
     */
    private String userType;

    /**
     * 上传日期
     */
    private String uploadDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 手机号(客户原始上传)
     */
    private String mobile;

    /**
     * 业务类型(客户原始上传)
     */
    private String biztype;

    /**
     * 是否黑名单
     */
    private String isBlack;

    /**
     * 是否转人工
     */
    private String isTurn;

    /**
     * 首登时间; yyyy-MM-dd HH:mm:ss（促首登）
     */
    private String clcUsrFstLogTimAll;

    /**
     * 最近一次登录时间; yyyy-MM-dd HH:mm:ss（促申完）
     */
    private String clcUsrLstAppStaTim;

    /**
     * 最近一次人脸识别完成（开始）时间; yyyy-MM-dd HH:mm:ss（促申完）
     */
    private String clcUsrIsoPhoTim;

    /**
     * 最近一次身份验证完成时间; yyyy-MM-dd HH:mm:ss;（促申完）
     */
    private String clcUsrIsoIdtTim;

    /**
     * 最近一次绑卡完成时间; yyyy-MM-dd HH:mm:ss（促申完）
     */
    private String clcUsrIsoCrdTim;

    /**
     * 最新一次个人信息验证完成时间; yyyy-MM-dd HH:mm:ss（促申完）
     */
    private String clcUsrIsoInfTim;

    /**
     * 最近一次申完时间; yyyy-MM-dd HH:mm:ss（促申完）
     */
    private String clcUsrIsoAtoTim;

    /**
     * 最近一次授信时间; yyyy-MM-dd HH:mm:ss（促申完）
     */
    private String clcUsrAdtTimRcnLon;

    /**
     * 用户授信额度区间; 格式：[0,3000] 返回 3k-,(3000,5000] 返回 3-5k,(5000,10000] 返回 5-10k,(10000,20000]返回 10-20k,(20000,30000]返回 20-30k,(30000,+∞]返回 30k+（促首借）
     */
    private String clcUsrAdtLmtItr;

    /**
     * 用户首次发起借款时间; yyyy-MM-dd HH:mm:ss（促首借）
     */
    private String clcUsrFrtFqOrdTim;

    /**
     * 用户首次借款成功时间; yyyy-MM-dd HH:mm:ss（促首借）
     */
    private String clcUsrFstLndTimCshBtHl;

    /**
     * 转化状态 0-无效、1-已转化、2-黑名单、3-人工（电销） 默认 0
     */
    private Integer isTransfer;

    /**
     * 异常信息
     */
    private String errorInfo;

    public CaseShuheUser() {
    }

    public CaseShuheUser(Long id, String apiCode, String custNum, String cell, String userType, String uploadDate, Date createTime, Date updateTime, String mobile, String biztype, String isBlack, String isTurn, String clcUsrFstLogTimAll, String clcUsrLstAppStaTim, String clcUsrIsoPhoTim, String clcUsrIsoIdtTim, String clcUsrIsoCrdTim, String clcUsrIsoInfTim, String clcUsrIsoAtoTim, String clcUsrAdtTimRcnLon, String clcUsrAdtLmtItr, String clcUsrFrtFqOrdTim, String clcUsrFstLndTimCshBtHl, Integer isTransfer, String errorInfo) {
        this.id = id;
        this.apiCode = apiCode;
        this.custNum = custNum;
        this.cell = cell;
        this.userType = userType;
        this.uploadDate = uploadDate;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.mobile = mobile;
        this.biztype = biztype;
        this.isBlack = isBlack;
        this.isTurn = isTurn;
        this.clcUsrFstLogTimAll = clcUsrFstLogTimAll;
        this.clcUsrLstAppStaTim = clcUsrLstAppStaTim;
        this.clcUsrIsoPhoTim = clcUsrIsoPhoTim;
        this.clcUsrIsoIdtTim = clcUsrIsoIdtTim;
        this.clcUsrIsoCrdTim = clcUsrIsoCrdTim;
        this.clcUsrIsoInfTim = clcUsrIsoInfTim;
        this.clcUsrIsoAtoTim = clcUsrIsoAtoTim;
        this.clcUsrAdtTimRcnLon = clcUsrAdtTimRcnLon;
        this.clcUsrAdtLmtItr = clcUsrAdtLmtItr;
        this.clcUsrFrtFqOrdTim = clcUsrFrtFqOrdTim;
        this.clcUsrFstLndTimCshBtHl = clcUsrFstLndTimCshBtHl;
        this.isTransfer = isTransfer;
        this.errorInfo = errorInfo;
    }

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

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum == null ? null : custNum.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate == null ? null : uploadDate.trim();
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public String getBiztype() {
        return biztype;
    }

    public void setBiztype(String biztype) {
        this.biztype = biztype == null ? null : biztype.trim();
    }

    public String getIsBlack() {
        return isBlack;
    }

    public void setIsBlack(String isBlack) {
        this.isBlack = isBlack == null ? null : isBlack.trim();
    }

    public String getIsTurn() {
        return isTurn;
    }

    public void setIsTurn(String isTurn) {
        this.isTurn = isTurn == null ? null : isTurn.trim();
    }

    public String getClcUsrFstLogTimAll() {
        return clcUsrFstLogTimAll;
    }

    public void setClcUsrFstLogTimAll(String clcUsrFstLogTimAll) {
        this.clcUsrFstLogTimAll = clcUsrFstLogTimAll == null ? null : clcUsrFstLogTimAll.trim();
    }

    public String getClcUsrLstAppStaTim() {
        return clcUsrLstAppStaTim;
    }

    public void setClcUsrLstAppStaTim(String clcUsrLstAppStaTim) {
        this.clcUsrLstAppStaTim = clcUsrLstAppStaTim == null ? null : clcUsrLstAppStaTim.trim();
    }

    public String getClcUsrIsoPhoTim() {
        return clcUsrIsoPhoTim;
    }

    public void setClcUsrIsoPhoTim(String clcUsrIsoPhoTim) {
        this.clcUsrIsoPhoTim = clcUsrIsoPhoTim == null ? null : clcUsrIsoPhoTim.trim();
    }

    public String getClcUsrIsoIdtTim() {
        return clcUsrIsoIdtTim;
    }

    public void setClcUsrIsoIdtTim(String clcUsrIsoIdtTim) {
        this.clcUsrIsoIdtTim = clcUsrIsoIdtTim == null ? null : clcUsrIsoIdtTim.trim();
    }

    public String getClcUsrIsoCrdTim() {
        return clcUsrIsoCrdTim;
    }

    public void setClcUsrIsoCrdTim(String clcUsrIsoCrdTim) {
        this.clcUsrIsoCrdTim = clcUsrIsoCrdTim == null ? null : clcUsrIsoCrdTim.trim();
    }

    public String getClcUsrIsoInfTim() {
        return clcUsrIsoInfTim;
    }

    public void setClcUsrIsoInfTim(String clcUsrIsoInfTim) {
        this.clcUsrIsoInfTim = clcUsrIsoInfTim == null ? null : clcUsrIsoInfTim.trim();
    }

    public String getClcUsrIsoAtoTim() {
        return clcUsrIsoAtoTim;
    }

    public void setClcUsrIsoAtoTim(String clcUsrIsoAtoTim) {
        this.clcUsrIsoAtoTim = clcUsrIsoAtoTim == null ? null : clcUsrIsoAtoTim.trim();
    }

    public String getClcUsrAdtTimRcnLon() {
        return clcUsrAdtTimRcnLon;
    }

    public void setClcUsrAdtTimRcnLon(String clcUsrAdtTimRcnLon) {
        this.clcUsrAdtTimRcnLon = clcUsrAdtTimRcnLon == null ? null : clcUsrAdtTimRcnLon.trim();
    }

    public String getClcUsrAdtLmtItr() {
        return clcUsrAdtLmtItr;
    }

    public void setClcUsrAdtLmtItr(String clcUsrAdtLmtItr) {
        this.clcUsrAdtLmtItr = clcUsrAdtLmtItr == null ? null : clcUsrAdtLmtItr.trim();
    }

    public String getClcUsrFrtFqOrdTim() {
        return clcUsrFrtFqOrdTim;
    }

    public void setClcUsrFrtFqOrdTim(String clcUsrFrtFqOrdTim) {
        this.clcUsrFrtFqOrdTim = clcUsrFrtFqOrdTim == null ? null : clcUsrFrtFqOrdTim.trim();
    }

    public String getClcUsrFstLndTimCshBtHl() {
        return clcUsrFstLndTimCshBtHl;
    }

    public void setClcUsrFstLndTimCshBtHl(String clcUsrFstLndTimCshBtHl) {
        this.clcUsrFstLndTimCshBtHl = clcUsrFstLndTimCshBtHl == null ? null : clcUsrFstLndTimCshBtHl.trim();
    }

    public Integer getIsTransfer() {
        return isTransfer;
    }

    public void setIsTransfer(Integer isTransfer) {
        this.isTransfer = isTransfer;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo == null ? null : errorInfo.trim();
    }
}