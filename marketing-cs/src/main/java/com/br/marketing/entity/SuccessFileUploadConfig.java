package com.br.marketing.entity;

import java.util.Date;

public class SuccessFileUploadConfig {
    /**
     * 主键
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 文件名包含规则，可含 yyyyMMdd/yyyy-MM-dd
     */
    private String fileName;

    /**
     * 文件上传间隔（分钟）
     */
    private Integer intervalMinutes;

    /**
     * 关联 b_sync_config 主键
     */
    private Long syncConfigId;

    /**
     * 状态：0-禁用 1-启用
     */
    private Byte status;

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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public Integer getIntervalMinutes() {
        return intervalMinutes;
    }

    public void setIntervalMinutes(Integer intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    public Long getSyncConfigId() {
        return syncConfigId;
    }

    public void setSyncConfigId(Long syncConfigId) {
        this.syncConfigId = syncConfigId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
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