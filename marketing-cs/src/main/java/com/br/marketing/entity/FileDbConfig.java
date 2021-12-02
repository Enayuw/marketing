package com.br.marketing.entity;

import java.util.Date;

public class FileDbConfig {
    /**
     * 
     */
    private Long id;

    /**
     * sftp配置表id
     */
    private Long sftpConfigId;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 内部地址
     */
    private String innerPath;

    /**
     * 表名
     */
    private String dbName;

    /**
     * 表字段集合 field指字段名，1-必填，0-非必填  格式如下——field1:1,field2:0)
     */
    private String dbFields;

    /**
     * 路由键
     */
    private String routeKey;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 删除标志 1-有效；9-无效
     */
    private Integer del;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSftpConfigId() {
        return sftpConfigId;
    }

    public void setSftpConfigId(Long sftpConfigId) {
        this.sftpConfigId = sftpConfigId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getInnerPath() {
        return innerPath;
    }

    public void setInnerPath(String innerPath) {
        this.innerPath = innerPath == null ? null : innerPath.trim();
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName == null ? null : dbName.trim();
    }

    public String getDbFields() {
        return dbFields;
    }

    public void setDbFields(String dbFields) {
        this.dbFields = dbFields == null ? null : dbFields.trim();
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey == null ? null : routeKey.trim();
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType == null ? null : fileType.trim();
    }

    public Integer getDel() {
        return del;
    }

    public void setDel(Integer del) {
        this.del = del;
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