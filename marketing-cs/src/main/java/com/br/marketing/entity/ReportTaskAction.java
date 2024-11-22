package com.br.marketing.entity;

import java.util.Date;

public class ReportTaskAction {
    /**
     * 
     */
    private Long id;

    /**
     * 报表任务id
     */
    private Long reportTaskId;

    /**
     * 任务状态 0-待执行；1-执行中；2-执行结束；
     */
    private Integer actionStatus;

    /**
     * 执行日期 格式： yyyy-MM-dd
     */
    private String actionDate;

    /**
     * 0-正常,1-逻辑删除
     */
    private Integer deleteFlag;

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

    public Long getReportTaskId() {
        return reportTaskId;
    }

    public void setReportTaskId(Long reportTaskId) {
        this.reportTaskId = reportTaskId;
    }

    public Integer getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(Integer actionStatus) {
        this.actionStatus = actionStatus;
    }

    public String getActionDate() {
        return actionDate;
    }

    public void setActionDate(String actionDate) {
        this.actionDate = actionDate == null ? null : actionDate.trim();
    }

    public Integer getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Integer deleteFlag) {
        this.deleteFlag = deleteFlag;
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