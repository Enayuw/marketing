package com.br.marketing.entity.dataProcess;

import com.br.marketing.entity.LocalFile;

import java.util.Date;

public class DataProcessingConfig {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 客户apiCode
     */
    private String apiCode;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件表头
     */
    private String fileHeader;

    /**
     * 数据分割符
     */
    private String dataSplit;

    /**
     * 代理类名称
     */
    private String proxyName;

    /**
     * url地址
     */
    private String url;

    /**
     * 优先级别（1最高，级别越高，数字越小）
     */
    private Byte priorityLevel;

    /**
     * 是否删除 0:否;1:是;
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 扩展字段
     */
    private String extendField;


    /**
     * localfile
     */
    private LocalFile localFile;

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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType == null ? null : fileType.trim();
    }

    public String getFileHeader() {
        return fileHeader;
    }

    public void setFileHeader(String fileHeader) {
        this.fileHeader = fileHeader == null ? null : fileHeader.trim();
    }

    public String getDataSplit() {
        return dataSplit;
    }

    public void setDataSplit(String dataSplit) {
        this.dataSplit = dataSplit == null ? null : dataSplit.trim();
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName == null ? null : proxyName.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Byte getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Byte priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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

    public String getExtendField() {
        return extendField;
    }

    public void setExtendField(String extendField) {
        this.extendField = extendField == null ? null : extendField.trim();
    }

    public LocalFile getLocalFile() {
        return localFile;
    }

    public void setLocalFile(LocalFile localFile) {
        this.localFile = localFile;
    }
}