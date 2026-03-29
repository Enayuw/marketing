package com.br.marketing.entity;

import java.util.Date;

public class MarketingDataFileConfigNoHeader {
    /**
     * 主键
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 按列顺序的字段配置JSON
     */
    private String fieldConfigColumn;

    /**
     * 转化清洗实现的服务名
     */
    private String transferServiceName;

    /**
     * 1-有效，9-删除
     */
    private Byte isDel;

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

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName == null ? null : ruleName.trim();
    }

    public String getFieldConfigColumn() {
        return fieldConfigColumn;
    }

    public void setFieldConfigColumn(String fieldConfigColumn) {
        this.fieldConfigColumn = fieldConfigColumn == null ? null : fieldConfigColumn.trim();
    }

    public String getTransferServiceName() {
        return transferServiceName;
    }

    public void setTransferServiceName(String transferServiceName) {
        this.transferServiceName = transferServiceName == null ? null : transferServiceName.trim();
    }

    public Byte getIsDel() {
        return isDel;
    }

    public void setIsDel(Byte isDel) {
        this.isDel = isDel;
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