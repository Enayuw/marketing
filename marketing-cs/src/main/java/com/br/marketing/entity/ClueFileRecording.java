package com.br.marketing.entity;

import java.util.Date;

public class ClueFileRecording {
    /**
     * 
     */
    private Long id;

    /**
     * 更新范围
     */
    private String updateScope;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件存储地址
     */
    private String fileAdress;

    /**
     * 文件状态：0-待清洗；1-清洗完成
     */
    private Integer fileCleanStatus;

    /**
     * 上传日期
     */
    private String appletDate;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUpdateScope() {
        return updateScope;
    }

    public void setUpdateScope(String updateScope) {
        this.updateScope = updateScope == null ? null : updateScope.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getFileAdress() {
        return fileAdress;
    }

    public void setFileAdress(String fileAdress) {
        this.fileAdress = fileAdress == null ? null : fileAdress.trim();
    }

    public Integer getFileCleanStatus() {
        return fileCleanStatus;
    }

    public void setFileCleanStatus(Integer fileCleanStatus) {
        this.fileCleanStatus = fileCleanStatus;
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
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
}