package com.br.marketing.entity;

import java.util.Date;

public class MarketingCleanDataTask {
    /**
     * 
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 规则配置id
     */
    private Long configId;

    /**
     * 文件id,多个用,分割
     */
    private String fileId;

    /**
     * 清洗类型：0上传，1转化
     */
    private Integer cleanType;

    /**
     * 试跑结果
     */
    private String testResult;

    /**
     * 清洗类型：0待清洗，1清洗中，2成功，3失败
     */
    private Integer cleanStatus;

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

    /**
     * 清洗方式：0 手动 (页面清洗任务对应的配置) 1 自动(程序清洗对应的配置)
     */
    private Integer autoCleanWayType;

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

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId == null ? null : fileId.trim();
    }

    public Integer getCleanType() {
        return cleanType;
    }

    public void setCleanType(Integer cleanType) {
        this.cleanType = cleanType;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult == null ? null : testResult.trim();
    }

    public Integer getCleanStatus() {
        return cleanStatus;
    }

    public void setCleanStatus(Integer cleanStatus) {
        this.cleanStatus = cleanStatus;
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

    public Integer getAutoCleanWayType() {
        return autoCleanWayType;
    }

    public void setAutoCleanWayType(Integer autoCleanWayType) {
        this.autoCleanWayType = autoCleanWayType;
    }
}