package com.br.marketing.entity;

import java.util.Date;

public class StraHisFile {
    /**
     * 
     */
    private Long id;

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

    /**
     * 2-成功;1-未跑分；
     */
    private Integer scoreStatus;

    /**
     * 统计文件地址
     */
    private String statisticFilePath;

    /**
     * 
     */
    private String showTitle;

    /**
     * 跑分结果推送类型，0文件，1 api，默认支持文件推送
     */
    private Integer pushType;

    /**
     * api数据推送状态 0 未推送  1推送成功 2 推送失败 3推送中
     */
    private Integer pushStatus;

    /**
     * 分片个数
     */
    private Integer indexNum;

    /**
     * 内部ftp地址
     */
    private String innerFtpPath;

    /**
     * 合并文件名称 多个文件用,分割
     */
    private String fileName;

    /**
     * 离线文件路径
     */
    private String offlineFilePath;

    /**
     * 跑分结束时间
     */
    private Date runningEndTime;

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

    public Integer getScoreStatus() {
        return scoreStatus;
    }

    public void setScoreStatus(Integer scoreStatus) {
        this.scoreStatus = scoreStatus;
    }

    public String getStatisticFilePath() {
        return statisticFilePath;
    }

    public void setStatisticFilePath(String statisticFilePath) {
        this.statisticFilePath = statisticFilePath == null ? null : statisticFilePath.trim();
    }

    public String getShowTitle() {
        return showTitle;
    }

    public void setShowTitle(String showTitle) {
        this.showTitle = showTitle == null ? null : showTitle.trim();
    }

    public Integer getPushType() {
        return pushType;
    }

    public void setPushType(Integer pushType) {
        this.pushType = pushType;
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    public Integer getIndexNum() {
        return indexNum;
    }

    public void setIndexNum(Integer indexNum) {
        this.indexNum = indexNum;
    }

    public String getInnerFtpPath() {
        return innerFtpPath;
    }

    public void setInnerFtpPath(String innerFtpPath) {
        this.innerFtpPath = innerFtpPath == null ? null : innerFtpPath.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getOfflineFilePath() {
        return offlineFilePath;
    }

    public void setOfflineFilePath(String offlineFilePath) {
        this.offlineFilePath = offlineFilePath == null ? null : offlineFilePath.trim();
    }

    public Date getRunningEndTime() {
        return runningEndTime;
    }

    public void setRunningEndTime(Date runningEndTime) {
        this.runningEndTime = runningEndTime;
    }
}