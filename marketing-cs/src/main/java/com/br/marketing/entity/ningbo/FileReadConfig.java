package com.br.marketing.entity.ningbo;

import java.util.Date;

public class FileReadConfig {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * APICODE
     */
    private String apiCode;

    /**
     * 配置名称，如“营销外呼数据V1”
     */
    private String configName;

    /**
     * 字段映射JSON配置。格式: {“文件字段名1”: “表字段名1”， “文件字段名2”: “表字段名2“}
     */
    private String fieldMapping;

    /**
     * 源文件分隔符，默认英文竖线
     */
    private String fileSeparator;

    /**
     * 源文件编码，默认UTF-8
     */
    private String fileCharset;

    /**
     * 状态: 0-禁用，1-启用
     */
    private Byte status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
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

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName == null ? null : configName.trim();
    }

    public String getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(String fieldMapping) {
        this.fieldMapping = fieldMapping == null ? null : fieldMapping.trim();
    }

    public String getFileSeparator() {
        return fileSeparator;
    }

    public void setFileSeparator(String fileSeparator) {
        this.fileSeparator = fileSeparator == null ? null : fileSeparator.trim();
    }

    public String getFileCharset() {
        return fileCharset;
    }

    public void setFileCharset(String fileCharset) {
        this.fileCharset = fileCharset == null ? null : fileCharset.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
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
}