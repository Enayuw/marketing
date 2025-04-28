package com.br.marketing.entity;

import java.util.Date;

public class MarketingDataCleanConfig {
    /**
     * 
     */
    private Long id;

    /**
     * 规则id
     */
    private String ruleId;

    /**
     * 匹配方式 1：映射；2：组合；3：默认值
     */
    private Integer mappingMode;

    /**
     * 来源类型 1：标准；2：扩展
     */
    private Integer originType;

    /**
     * 目标类型 1：标准；2：扩展
     */
    private Integer targetType;

    /**
     * 来源key，mapping_mode=1时必填
     */
    private String originName;

    /**
     * 目标key
     */
    private String targetName;

    /**
     * 枚举值映射，可用于mapping_mode=1
     */
    private String conversion;

    /**
     * 默认值，可用于mapping_mode=3
     */
    private String defaultValue;

    /**
     * 时间转换格式
     */
    private String dateTransformPattern;

    /**
     * 保留类型 0：四舍五入；1：舍位
     */
    private Integer decimalReserveType;

    /**
     * 保留位数
     */
    private Integer decimalReservePrecision;

    /**
     * 组合条件
     */
    private String mappingCondition;

    /**
     * 组合输出
     */
    private String mappingOutValue;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

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

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId == null ? null : ruleId.trim();
    }

    public Integer getMappingMode() {
        return mappingMode;
    }

    public void setMappingMode(Integer mappingMode) {
        this.mappingMode = mappingMode;
    }

    public Integer getOriginType() {
        return originType;
    }

    public void setOriginType(Integer originType) {
        this.originType = originType;
    }

    public Integer getTargetType() {
        return targetType;
    }

    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName == null ? null : originName.trim();
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName == null ? null : targetName.trim();
    }

    public String getConversion() {
        return conversion;
    }

    public void setConversion(String conversion) {
        this.conversion = conversion == null ? null : conversion.trim();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue == null ? null : defaultValue.trim();
    }

    public String getDateTransformPattern() {
        return dateTransformPattern;
    }

    public void setDateTransformPattern(String dateTransformPattern) {
        this.dateTransformPattern = dateTransformPattern == null ? null : dateTransformPattern.trim();
    }

    public Integer getDecimalReserveType() {
        return decimalReserveType;
    }

    public void setDecimalReserveType(Integer decimalReserveType) {
        this.decimalReserveType = decimalReserveType;
    }

    public Integer getDecimalReservePrecision() {
        return decimalReservePrecision;
    }

    public void setDecimalReservePrecision(Integer decimalReservePrecision) {
        this.decimalReservePrecision = decimalReservePrecision;
    }

    public String getMappingCondition() {
        return mappingCondition;
    }

    public void setMappingCondition(String mappingCondition) {
        this.mappingCondition = mappingCondition == null ? null : mappingCondition.trim();
    }

    public String getMappingOutValue() {
        return mappingOutValue;
    }

    public void setMappingOutValue(String mappingOutValue) {
        this.mappingOutValue = mappingOutValue == null ? null : mappingOutValue.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
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