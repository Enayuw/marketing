package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class XieChengStatisticsReport {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 日期T-1
     */
    private String reportTime;

    /**
     * 上报量级
     */
    private String uploadCount;

    /**
     * 上报进入首页量级
     */
    private String reportedHomePageCount;

    /**
     * 上报进件发起量级
     */
    private String reportedInitiateCount;

    /**
     * 上报进件成功量级
     */
    private String reportedSuccessCount;

    /**
     * 上报授信量级
     */
    private String reportedCreditCount;

    /**
     * 上报提现量级
     */
    private String reportedDrawingsCount;

    /**
     * 上报百万量级授信量
     */
    private String reportedMillionCreditCount;

    /**
     * 外呼量级
     */
    private String outboundCount;

    /**
     * 外呼进入首页量级
     */
    private String outboundHomePageCount;

    /**
     * 外呼进件发起量级
     */
    private String outboundInitiateCount;

    /**
     * 外呼进件成功量级
     */
    private String outboundSuccessCount;

    /**
     * 外呼授信量级
     */
    private String outboundCreditCount;

    /**
     * 外呼提现量级
     */
    private String outboundDrawingsCount;

    /**
     * 外呼百万量级授信量
     */
    private String outboundMillionCreditCount;

    /**
     * 描述
     */
    private String remark;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime == null ? null : reportTime.trim();
    }

    public String getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(String uploadCount) {
        this.uploadCount = uploadCount == null ? null : uploadCount.trim();
    }

    public String getReportedHomePageCount() {
        return reportedHomePageCount;
    }

    public void setReportedHomePageCount(String reportedHomePageCount) {
        this.reportedHomePageCount = reportedHomePageCount == null ? null : reportedHomePageCount.trim();
    }

    public String getReportedInitiateCount() {
        return reportedInitiateCount;
    }

    public void setReportedInitiateCount(String reportedInitiateCount) {
        this.reportedInitiateCount = reportedInitiateCount == null ? null : reportedInitiateCount.trim();
    }

    public String getReportedSuccessCount() {
        return reportedSuccessCount;
    }

    public void setReportedSuccessCount(String reportedSuccessCount) {
        this.reportedSuccessCount = reportedSuccessCount == null ? null : reportedSuccessCount.trim();
    }

    public String getReportedCreditCount() {
        return reportedCreditCount;
    }

    public void setReportedCreditCount(String reportedCreditCount) {
        this.reportedCreditCount = reportedCreditCount == null ? null : reportedCreditCount.trim();
    }

    public String getReportedDrawingsCount() {
        return reportedDrawingsCount;
    }

    public void setReportedDrawingsCount(String reportedDrawingsCount) {
        this.reportedDrawingsCount = reportedDrawingsCount == null ? null : reportedDrawingsCount.trim();
    }

    public String getReportedMillionCreditCount() {
        return reportedMillionCreditCount;
    }

    public void setReportedMillionCreditCount(String reportedMillionCreditCount) {
        this.reportedMillionCreditCount = reportedMillionCreditCount == null ? null : reportedMillionCreditCount.trim();
    }

    public String getOutboundCount() {
        return outboundCount;
    }

    public void setOutboundCount(String outboundCount) {
        this.outboundCount = outboundCount == null ? null : outboundCount.trim();
    }

    public String getOutboundHomePageCount() {
        return outboundHomePageCount;
    }

    public void setOutboundHomePageCount(String outboundHomePageCount) {
        this.outboundHomePageCount = outboundHomePageCount == null ? null : outboundHomePageCount.trim();
    }

    public String getOutboundInitiateCount() {
        return outboundInitiateCount;
    }

    public void setOutboundInitiateCount(String outboundInitiateCount) {
        this.outboundInitiateCount = outboundInitiateCount == null ? null : outboundInitiateCount.trim();
    }

    public String getOutboundSuccessCount() {
        return outboundSuccessCount;
    }

    public void setOutboundSuccessCount(String outboundSuccessCount) {
        this.outboundSuccessCount = outboundSuccessCount == null ? null : outboundSuccessCount.trim();
    }

    public String getOutboundCreditCount() {
        return outboundCreditCount;
    }

    public void setOutboundCreditCount(String outboundCreditCount) {
        this.outboundCreditCount = outboundCreditCount == null ? null : outboundCreditCount.trim();
    }

    public String getOutboundDrawingsCount() {
        return outboundDrawingsCount;
    }

    public void setOutboundDrawingsCount(String outboundDrawingsCount) {
        this.outboundDrawingsCount = outboundDrawingsCount == null ? null : outboundDrawingsCount.trim();
    }

    public String getOutboundMillionCreditCount() {
        return outboundMillionCreditCount;
    }

    public void setOutboundMillionCreditCount(String outboundMillionCreditCount) {
        this.outboundMillionCreditCount = outboundMillionCreditCount == null ? null : outboundMillionCreditCount.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}