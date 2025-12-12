package com.br.marketing.entity;

import java.util.Date;

public class LocalFile {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String cid;

    /**
     * 
     */
    private String apiCode;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 源地址
     */
    private String srcPath;

    /**
     * 
     */
    private String fileName;

    /**
     * 本地地址
     */
    private String localPath;

    /**
     * 状态，1-处理中，2-处理完成
     */
    private String status;

    /**
     * 状态，1校验通过，2表头有问题，3数据有问题，4-文件内容为空
     */
    private String complete;

    /**
     * 
     */
    private Integer actualNumber;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 上传错误数据数量
     */
    private Integer errorActualNumber;

    /**
     * 推送数据数量
     */
    private Integer pushNumber;

    /**
     * 推送数据开始是时间
     */
    private Date pushStartTime;

    /**
     * 推送数据结束时间
     */
    private Date pushEndTime;

    /**
     * 推送状态 0-推送中,1-推送中,2-部分成功,3-推送成功,4-推送失败
     */
    private String pushStatus;

    /**
     * 错误信息描述
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType == null ? null : fileType.trim();
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath == null ? null : srcPath.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath == null ? null : localPath.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getComplete() {
        return complete;
    }

    public void setComplete(String complete) {
        this.complete = complete == null ? null : complete.trim();
    }

    public Integer getActualNumber() {
        return actualNumber;
    }

    public void setActualNumber(Integer actualNumber) {
        this.actualNumber = actualNumber;
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

    public Integer getErrorActualNumber() {
        return errorActualNumber;
    }

    public void setErrorActualNumber(Integer errorActualNumber) {
        this.errorActualNumber = errorActualNumber;
    }

    public Integer getPushNumber() {
        return pushNumber;
    }

    public void setPushNumber(Integer pushNumber) {
        this.pushNumber = pushNumber;
    }

    public Date getPushStartTime() {
        return pushStartTime;
    }

    public void setPushStartTime(Date pushStartTime) {
        this.pushStartTime = pushStartTime;
    }

    public Date getPushEndTime() {
        return pushEndTime;
    }

    public void setPushEndTime(Date pushEndTime) {
        this.pushEndTime = pushEndTime;
    }

    public String getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(String pushStatus) {
        this.pushStatus = pushStatus == null ? null : pushStatus.trim();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? null : errorMessage.trim();
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}