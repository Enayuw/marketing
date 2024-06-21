package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * b_marketing_data_file_config
 * @author 
 */
public class MarketingDataFileConfig implements Serializable {
    private Long id;

    private String apiCode;

    /**
     * 匹配文件Id
     */
    private Integer fileId;

    /**
     * 清洗类型：0上传，1转化
     */
    private Integer cleanType;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 字段配置
     */
    private String fieldConfig;

    /**
     * 字段配置展示
     */
    private String fieldConfigShow;

    /**
     * 实现的服务名
     */
    private String serviceName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 1-有效；9-删除
     */
    private Integer isDel;

    /**
     * 是否校验表名 0校验 1不校验
     */
    private Byte isChecklistName;

    /**
     * 表名校验规则
     */
    private String validationRules;

    private static final long serialVersionUID = 1L;

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
        this.apiCode = apiCode;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getCleanType() {
        return cleanType;
    }

    public void setCleanType(Integer cleanType) {
        this.cleanType = cleanType;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getFieldConfig() {
        return fieldConfig;
    }

    public void setFieldConfig(String fieldConfig) {
        this.fieldConfig = fieldConfig;
    }

    public String getFieldConfigShow() {
        return fieldConfigShow;
    }

    public void setFieldConfigShow(String fieldConfigShow) {
        this.fieldConfigShow = fieldConfigShow;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public Byte getIsChecklistName() {
        return isChecklistName;
    }

    public void setIsChecklistName(Byte isChecklistName) {
        this.isChecklistName = isChecklistName;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }
}