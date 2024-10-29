package com.br.marketing.entity;

import java.util.Date;

public class ReportStatisticTransfer {
    /**
     * id
     */
    private Long id;

    /**
     * 报表id
     */
    private String reportId;

    /**
     * 报表任务id
     */
    private String reportTaskId;

    /**
     * 报表类型
     */
    private String reportType;

    /**
     * 模型字段
     */
    private String scoreField;

    /**
     * 维度
     */
    private String dimensionField;

    /**
     * 维度值
     */
    private String dimensionValue;

    /**
     * 多头字段
     */
    private String multiHeadField;

    /**
     * 报表状态 1-统计成功；2-统计失败
     */
    private String reportStatus;

    /**
     * 统计日期 格式yyyy-MM-dd
     */
    private String reportDate;

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

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId == null ? null : reportId.trim();
    }

    public String getReportTaskId() {
        return reportTaskId;
    }

    public void setReportTaskId(String reportTaskId) {
        this.reportTaskId = reportTaskId == null ? null : reportTaskId.trim();
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType == null ? null : reportType.trim();
    }

    public String getScoreField() {
        return scoreField;
    }

    public void setScoreField(String scoreField) {
        this.scoreField = scoreField == null ? null : scoreField.trim();
    }

    public String getDimensionField() {
        return dimensionField;
    }

    public void setDimensionField(String dimensionField) {
        this.dimensionField = dimensionField == null ? null : dimensionField.trim();
    }

    public String getDimensionValue() {
        return dimensionValue;
    }

    public void setDimensionValue(String dimensionValue) {
        this.dimensionValue = dimensionValue == null ? null : dimensionValue.trim();
    }

    public String getMultiHeadField() {
        return multiHeadField;
    }

    public void setMultiHeadField(String multiHeadField) {
        this.multiHeadField = multiHeadField == null ? null : multiHeadField.trim();
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus == null ? null : reportStatus.trim();
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate == null ? null : reportDate.trim();
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