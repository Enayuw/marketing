package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ReportTask  implements Serializable {
    /**
     * 
     */
    private Long id;

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * 报表规则集
     */
    private String reportRules;

    /**
     * 文件下载地址
     */
    private String downloadUrl;

    /**
     * 状态 0-待开始；1-统计中；2-已完成；3-统计失败
     */
    private Integer status;

    /**
     * 下载状态 0-未生成；1-文件生成中；2-文件已生成；
     */
    private Integer downStatus;

    /**
     * 报表类型1-跑分模型分布
     */
    private Integer reportType;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

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

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName == null ? null : reportName.trim();
    }

    public String getReportRules() {
        return reportRules;
    }

    public void setReportRules(String reportRules) {
        this.reportRules = reportRules == null ? null : reportRules.trim();
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl == null ? null : downloadUrl.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDownStatus() {
        return downStatus;
    }

    public void setDownStatus(Integer downStatus) {
        this.downStatus = downStatus;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
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