package com.br.marketing.entity;

import java.util.Date;

public class CallRecordLog {
    /**
     *
     */
    private Long id;

    /**
     * 通话明细id
     */
    private Long recordId;

    /**
     *
     */
    private String apiCode;

    /**
     * 案件编号
     */
    private String caseNum;

    /**
     * 意向等级 A级(有明确意向）;B级(可能有意向);C级(明确拒绝);D级(用户忙);E级(拨打失败);F级(无效客户)
     */
    private String intentionGrade;

    /**
     * 入库状态：0-未入库、1-入库中、2-入库成功、3-入库失败
     */
    private Integer inboundStatus;

    /**
     * 入库日期
     */
    private String appletDate;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getCaseNum() {
        return caseNum;
    }

    public void setCaseNum(String caseNum) {
        this.caseNum = caseNum == null ? null : caseNum.trim();
    }

    public String getIntentionGrade() {
        return intentionGrade;
    }

    public void setIntentionGrade(String intentionGrade) {
        this.intentionGrade = intentionGrade == null ? null : intentionGrade.trim();
    }

    public Integer getInboundStatus() {
        return inboundStatus;
    }

    public void setInboundStatus(Integer inboundStatus) {
        this.inboundStatus = inboundStatus;
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? null : errorMessage.trim();
    }
}