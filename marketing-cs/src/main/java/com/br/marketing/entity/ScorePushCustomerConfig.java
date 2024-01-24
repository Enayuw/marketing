package com.br.marketing.entity;

import java.util.Date;

public class ScorePushCustomerConfig {
    /**
     * 
     */
    private Long id;

    /**
     * apicode
     */
    private String apiCode;

    /**
     * 跑分规则编号
     */
    private String scoreRuleShortName;

    /**
     * 字段映射
     */
    private String fieldMapping;

    /**
     * 分数映射字段1
     */
    private String scoreSort1Mapping;

    /**
     * 分数映射字段2
     */
    private String scoreSort2Mapping;

    /**
     * 分数映射字段3
     */
    private String scoreSort3Mapping;

    /**
     * 分数映射字段4
     */
    private String scoreSort4Mapping;

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

    /**
     * 推送类型 1-一天仅推送1次;2-多文件推送多次
     */
    private Integer pushType;

    /**
     * 推送方法
     */
    private String pushMethod;

    /**
     * 资源配置
     */
    private String resourceConfig;

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

    public String getScoreRuleShortName() {
        return scoreRuleShortName;
    }

    public void setScoreRuleShortName(String scoreRuleShortName) {
        this.scoreRuleShortName = scoreRuleShortName == null ? null : scoreRuleShortName.trim();
    }

    public String getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(String fieldMapping) {
        this.fieldMapping = fieldMapping == null ? null : fieldMapping.trim();
    }

    public String getScoreSort1Mapping() {
        return scoreSort1Mapping;
    }

    public void setScoreSort1Mapping(String scoreSort1Mapping) {
        this.scoreSort1Mapping = scoreSort1Mapping == null ? null : scoreSort1Mapping.trim();
    }

    public String getScoreSort2Mapping() {
        return scoreSort2Mapping;
    }

    public void setScoreSort2Mapping(String scoreSort2Mapping) {
        this.scoreSort2Mapping = scoreSort2Mapping == null ? null : scoreSort2Mapping.trim();
    }

    public String getScoreSort3Mapping() {
        return scoreSort3Mapping;
    }

    public void setScoreSort3Mapping(String scoreSort3Mapping) {
        this.scoreSort3Mapping = scoreSort3Mapping == null ? null : scoreSort3Mapping.trim();
    }

    public String getScoreSort4Mapping() {
        return scoreSort4Mapping;
    }

    public void setScoreSort4Mapping(String scoreSort4Mapping) {
        this.scoreSort4Mapping = scoreSort4Mapping == null ? null : scoreSort4Mapping.trim();
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

    public Integer getPushType() {
        return pushType;
    }

    public void setPushType(Integer pushType) {
        this.pushType = pushType;
    }

    public String getPushMethod() {
        return pushMethod;
    }

    public void setPushMethod(String pushMethod) {
        this.pushMethod = pushMethod == null ? null : pushMethod.trim();
    }

    public String getResourceConfig() {
        return resourceConfig;
    }

    public void setResourceConfig(String resourceConfig) {
        this.resourceConfig = resourceConfig == null ? null : resourceConfig.trim();
    }
}