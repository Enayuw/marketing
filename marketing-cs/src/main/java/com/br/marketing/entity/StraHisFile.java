package com.br.marketing.entity;

import java.util.Date;

public class StraHisFile {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 文件全路径
     */
    private String filePath;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 1：文件未上传，0：文件已上传至ftp，2：文件已同步至客户sftp
     */
    private Integer status;

    /**
     * 类型：0增量，1全量，2一次性
     */
    private Integer type;

    /**
     * 文件名称
     */
    private String zipfileName;

    /**
     * 错误文件名称
     */
    private String errorFile;

    /**
     * 结果文件应有行数
     */
    private Integer expectedNum;

    /**
     * 文件上传时间
     */
    private String uploadTime;

    /**
     * 结果文件大小
     */
    private String fileSize;

    /**
     * 结果文件实际行数
     */
    private Integer actualNum;

    /**
     * 实际返回的文件个数
     */
    private Integer fileNum;

    /**
     * 回传标识文件.finish状态：1,可上传回传标识文件，2，存在异常不能上传回传标志文件
     */
    private Integer signFileStatus;

    /**
     * 压缩文件状态，1正常，2异常
     */
    private Integer zipStatus;

    /**
     * 文件MD5
     */
    private String md5;

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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? null : filePath.trim();
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getZipfileName() {
        return zipfileName;
    }

    public void setZipfileName(String zipfileName) {
        this.zipfileName = zipfileName == null ? null : zipfileName.trim();
    }

    public String getErrorFile() {
        return errorFile;
    }

    public void setErrorFile(String errorFile) {
        this.errorFile = errorFile == null ? null : errorFile.trim();
    }

    public Integer getExpectedNum() {
        return expectedNum;
    }

    public void setExpectedNum(Integer expectedNum) {
        this.expectedNum = expectedNum;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime == null ? null : uploadTime.trim();
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize == null ? null : fileSize.trim();
    }

    public Integer getActualNum() {
        return actualNum;
    }

    public void setActualNum(Integer actualNum) {
        this.actualNum = actualNum;
    }

    public Integer getFileNum() {
        return fileNum;
    }

    public void setFileNum(Integer fileNum) {
        this.fileNum = fileNum;
    }

    public Integer getSignFileStatus() {
        return signFileStatus;
    }

    public void setSignFileStatus(Integer signFileStatus) {
        this.signFileStatus = signFileStatus;
    }

    public Integer getZipStatus() {
        return zipStatus;
    }

    public void setZipStatus(Integer zipStatus) {
        this.zipStatus = zipStatus;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5 == null ? null : md5.trim();
    }
}