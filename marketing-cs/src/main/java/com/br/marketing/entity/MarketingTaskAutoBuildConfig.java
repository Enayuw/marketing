package com.br.marketing.entity;

import java.util.Date;

public class MarketingTaskAutoBuildConfig {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 跑分规则配置表id
     */
    private Integer scoreRuleId;

    /**
     * 上传记录表ids(逗号分割)
     */
    private String syncReportId;

    /**
     * 数据范围
     */
    private String dataCondition;

    /**
     * 开始日期(yyyy-MM-dd)
     */
    private String startDate;

    /**
     * 开始时间(HH:mm)
     */
    private String startTime;

    /**
     * 截止日期(yyyy-MM-dd)
     */
    private String closeDate;

    /**
     * 周期天数(每隔几天生成任务)
     */
    private Integer cycleDay;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 标签名称
     */
    private String labelName;

    /**
     * 是否删除 0:否;1:是;
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

    public Integer getScoreRuleId() {
        return scoreRuleId;
    }

    public void setScoreRuleId(Integer scoreRuleId) {
        this.scoreRuleId = scoreRuleId;
    }

    public String getSyncReportId() {
        return syncReportId;
    }

    public void setSyncReportId(String syncReportId) {
        this.syncReportId = syncReportId == null ? null : syncReportId.trim();
    }

    public String getDataCondition() {
        return dataCondition;
    }

    public void setDataCondition(String dataCondition) {
        this.dataCondition = dataCondition == null ? null : dataCondition.trim();
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate == null ? null : startDate.trim();
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime == null ? null : startTime.trim();
    }

    public String getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(String closeDate) {
        this.closeDate = closeDate == null ? null : closeDate.trim();
    }

    public Integer getCycleDay() {
        return cycleDay;
    }

    public void setCycleDay(Integer cycleDay) {
        this.cycleDay = cycleDay;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName == null ? null : labelName.trim();
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