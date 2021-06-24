package com.br.marketing.entity;

import java.util.Date;

public class SyncLog {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 读取文件的目录
     */
    private String srcPath;

    /**
     * 写入文件的目录
     */
    private String targetPath;

    /**
     * 同步的文件名称
     */
    private String fileName;

    /**
     * 
     */
    private String fileSize;

    /**
     * 文件生成的时间
     */
    private String createFileTime;

    /**
     * 开始同步的时间
     */
    private String startTime;

    /**
     * 同步结束的时间
     */
    private String endTime;

    /**
     * 状态   1：同步成功，2：同步失败
     */
    private Integer status;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 客户批次号
     */
    private String cusBatchNumber;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath == null ? null : srcPath.trim();
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath == null ? null : targetPath.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize == null ? null : fileSize.trim();
    }

    public String getCreateFileTime() {
        return createFileTime;
    }

    public void setCreateFileTime(String createFileTime) {
        this.createFileTime = createFileTime == null ? null : createFileTime.trim();
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime == null ? null : startTime.trim();
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime == null ? null : endTime.trim();
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

    public String getCusBatchNumber() {
        return cusBatchNumber;
    }

    public void setCusBatchNumber(String cusBatchNumber) {
        this.cusBatchNumber = cusBatchNumber == null ? null : cusBatchNumber.trim();
    }
}