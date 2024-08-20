package com.br.marketing.entity.clean;

import java.util.Date;

public class MarketingCleanCreateTaskRule {
    /**
     *
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * sftp账号配置id
     */
    private Long syncConfigId;

    /**
     * 数据类型：1-文件数据，2-表数据
     */
    private Integer dataType;

    /**
     * 规则配置id
     */
    private Long marketingDataFileConfigId;

    /**
     * 数据操纵语言：1-INSERT，2-UPDATE，3-DELETE
     */
    private Integer dml;

    /**
     * 开始时间，格式HH:mm[:ss]
     */
    private String startTime;

    /**
     * 任务创建规则：1-单次，2-周期
     */
    private Integer taskCreateRule;

    /**
     * 周期天，生成指定日期的清洗任务
     */
    private Integer cycleDay;

    /**
     * 压缩密码
     */
    private String zipPassword;

    /**
     * 开启md5值检查：0-不开启，1-开启
     */
    private Integer isMd5Check;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态：0-未删除；1-已删除
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

    public Long getSyncConfigId() {
        return syncConfigId;
    }

    public void setSyncConfigId(Long syncConfigId) {
        this.syncConfigId = syncConfigId;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Long getMarketingDataFileConfigId() {
        return marketingDataFileConfigId;
    }

    public void setMarketingDataFileConfigId(Long marketingDataFileConfigId) {
        this.marketingDataFileConfigId = marketingDataFileConfigId;
    }

    public Integer getDml() {
        return dml;
    }

    public void setDml(Integer dml) {
        this.dml = dml;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime == null ? null : startTime.trim();
    }

    public Integer getTaskCreateRule() {
        return taskCreateRule;
    }

    public void setTaskCreateRule(Integer taskCreateRule) {
        this.taskCreateRule = taskCreateRule;
    }

    public Integer getCycleDay() {
        return cycleDay;
    }

    public void setCycleDay(Integer cycleDay) {
        this.cycleDay = cycleDay;
    }

    public String getZipPassword() {
        return zipPassword;
    }

    public void setZipPassword(String zipPassword) {
        this.zipPassword = zipPassword == null ? null : zipPassword.trim();
    }

    public Integer getIsMd5Check() {
        return isMd5Check;
    }

    public void setIsMd5Check(Integer isMd5Check) {
        this.isMd5Check = isMd5Check;
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