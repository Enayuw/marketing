package com.br.marketing.entity;

import java.util.Date;

public class PullCustomerFileData {
    /**
     *
     */
    private Long id;

    /**
     * 客户编号
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localFileId;

    /**
     * 文本内容
     */
    private String fileData;

    /**
     * json内容
     */
    private String jsonData;

    /**
     * 数据状态: 1-正常 2-异常
     */
    private Integer dataStatus;

    /**
     * 数据描述
     */
    private String dataDesc;

    /**
     * 数据指纹
     */
    private String dataFingerprint;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建日期，格式：yyyy-MM-dd
     */
    private String createDate;

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

    public Long getLocalFileId() {
        return localFileId;
    }

    public void setLocalFileId(Long localFileId) {
        this.localFileId = localFileId;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData == null ? null : fileData.trim();
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData == null ? null : jsonData.trim();
    }

    public Integer getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(Integer dataStatus) {
        this.dataStatus = dataStatus;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public void setDataDesc(String dataDesc) {
        this.dataDesc = dataDesc == null ? null : dataDesc.trim();
    }

    public String getDataFingerprint() {
        return dataFingerprint;
    }

    public void setDataFingerprint(String dataFingerprint) {
        this.dataFingerprint = dataFingerprint == null ? null : dataFingerprint.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate == null ? null : createDate.trim();
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