package com.br.marketing.entity;

import java.util.Date;

public class BFileBiConfig {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 1-转化提取文件同步至marketing-bi
     * 9-宜信邮件读取任务的类型
     */
    private String busType;

    /**
     * 表名
     */
    private String dbName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 表字段集合
     */
    private String dbFields;

    /**
     * 表字段映射
     */
    private String dbColFieldsMap;

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

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType == null ? null : busType.trim();
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName == null ? null : dbName.trim();
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

    public String getDbFields() {
        return dbFields;
    }

    public void setDbFields(String dbFields) {
        this.dbFields = dbFields == null ? null : dbFields.trim();
    }

    public String getDbColFieldsMap() {
        return dbColFieldsMap;
    }

    public void setDbColFieldsMap(String dbColFieldsMap) {
        this.dbColFieldsMap = dbColFieldsMap == null ? null : dbColFieldsMap.trim();
    }
}