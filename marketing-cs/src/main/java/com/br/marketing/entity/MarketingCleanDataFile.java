package com.br.marketing.entity;

import java.util.Date;

public class MarketingCleanDataFile {
    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 清洗类型：0上传，1转化
     */
    private Integer cleanType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件表头
     */
    private String fileHeader;

    /**
     * 文件数据
     */
    private String fileData;

    /**
     * 目标sftp路径
     */
    private String targetSftpPath;

    /**
     * 本地文件路径
     */
    private String localPath;

    /**
     * 状态
     */
    private Integer status;

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
     * sftp账号配置id
     */
    private Long syncConfigId;

    /**
     * 文件MD5值
     */
    private String md5Value;

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

    public Integer getCleanType() {
        return cleanType;
    }

    public void setCleanType(Integer cleanType) {
        this.cleanType = cleanType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getFileHeader() {
        return fileHeader;
    }

    public void setFileHeader(String fileHeader) {
        this.fileHeader = fileHeader == null ? null : fileHeader.trim();
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData == null ? null : fileData.trim();
    }

    public String getTargetSftpPath() {
        return targetSftpPath;
    }

    public void setTargetSftpPath(String targetSftpPath) {
        this.targetSftpPath = targetSftpPath == null ? null : targetSftpPath.trim();
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath == null ? null : localPath.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Long getSyncConfigId() {
        return syncConfigId;
    }

    public void setSyncConfigId(Long syncConfigId) {
        this.syncConfigId = syncConfigId;
    }

    public String getMd5Value() {
        return md5Value;
    }

    public void setMd5Value(String md5Value) {
        this.md5Value = md5Value == null ? null : md5Value.trim();
    }
}