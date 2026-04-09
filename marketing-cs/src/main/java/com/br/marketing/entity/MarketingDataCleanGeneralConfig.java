package com.br.marketing.entity;

import java.util.Date;

public class MarketingDataCleanGeneralConfig {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * API编码
     */
    private String apiCode;

    /**
     * 账号类型
     */
    private String accountType;

    /**
     * 数据来源,0-营销中台 1-外呼系统
     */
    private Integer systemType;

    /**
     * 数据类型：0:上传，1:转化
     */
    private Integer dataType;

    /**
     * 接收类型：0:通用,1:定制,2:FTP
     */
    private Integer acceptType;

    /**
     * 定制清洗配置运行状态：0:未运行,1:运行中
     */
    private Integer customRunStatus;

    /**
     * 规则状态：0:待生效,1:试跑成功
     */
    private Integer status;

    /**
     * SFTP路径
     */
    private String sftpPath;

    /**
     * SFTP 文件分隔符
     */
    private String sftpFileSeparator;

    /**
     * 是否删除：1-正常；9-删除
     */
    private Integer isDel;

    /**
     * 操作人id
     */
    private Long optUserId;

    /**
     * 操作人账户名
     */
    private String optUserName;

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

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType == null ? null : accountType.trim();
    }

    public Integer getSystemType() {
        return systemType;
    }

    public void setSystemType(Integer systemType) {
        this.systemType = systemType;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getAcceptType() {
        return acceptType;
    }

    public void setAcceptType(Integer acceptType) {
        this.acceptType = acceptType;
    }

    public Integer getCustomRunStatus() {
        return customRunStatus;
    }

    public void setCustomRunStatus(Integer customRunStatus) {
        this.customRunStatus = customRunStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSftpPath() {
        return sftpPath;
    }

    public void setSftpPath(String sftpPath) {
        this.sftpPath = sftpPath == null ? null : sftpPath.trim();
    }

    public String getSftpFileSeparator() {
        return sftpFileSeparator;
    }

    public void setSftpFileSeparator(String sftpFileSeparator) {
        this.sftpFileSeparator = sftpFileSeparator == null ? null : sftpFileSeparator.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Long getOptUserId() {
        return optUserId;
    }

    public void setOptUserId(Long optUserId) {
        this.optUserId = optUserId;
    }

    public String getOptUserName() {
        return optUserName;
    }

    public void setOptUserName(String optUserName) {
        this.optUserName = optUserName == null ? null : optUserName.trim();
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