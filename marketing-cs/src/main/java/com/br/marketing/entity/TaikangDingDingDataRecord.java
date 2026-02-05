package com.br.marketing.entity;

public class TaikangDingDingDataRecord {
    /**
     * 
     */
    private Long id;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 外呼时间
     */
    private String browseDate;

    /**
     * 姓名
     */
    private String applicationName;

    /**
     * return_result1
     */
    private String returnResult1;

    /**
     * 创建人unionId
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private String createdTime;

    /**
     * 最近修改人unionId
     */
    private String lastModifiedBy;

    /**
     * 最近修改时间
     */
    private String lastModifiedTime;

    /**
     * 最近修改人userId
     */
    private String lastModifiedUserId;

    /**
     * 最近修改人name
     */
    private String lastModifiedUserName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getBrowseDate() {
        return browseDate;
    }

    public void setBrowseDate(String browseDate) {
        this.browseDate = browseDate == null ? null : browseDate.trim();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName == null ? null : applicationName.trim();
    }

    public String getReturnResult1() {
        return returnResult1;
    }

    public void setReturnResult1(String returnResult1) {
        this.returnResult1 = returnResult1 == null ? null : returnResult1.trim();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime == null ? null : createdTime.trim();
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy == null ? null : lastModifiedBy.trim();
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime == null ? null : lastModifiedTime.trim();
    }

    public String getLastModifiedUserId() {
        return lastModifiedUserId;
    }

    public void setLastModifiedUserId(String lastModifiedUserId) {
        this.lastModifiedUserId = lastModifiedUserId == null ? null : lastModifiedUserId.trim();
    }

    public String getLastModifiedUserName() {
        return lastModifiedUserName;
    }

    public void setLastModifiedUserName(String lastModifiedUserName) {
        this.lastModifiedUserName = lastModifiedUserName == null ? null : lastModifiedUserName.trim();
    }
}