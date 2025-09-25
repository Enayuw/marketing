package com.br.marketing.entity;

import java.util.Date;

public class PushDecisions {
    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 规则编号
     */
    private String ruleNumber;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 依赖模板id
     */
    private Long dependencyTemplateId;

    /**
     * 规则状态 1-启用;2-禁用
     */
    private Integer status;

    /**
     * 每日自动执行时间
     */
    private String autoTime;

    /**
     * 推送数据集
     */
    private String pushDatasets;

    /**
     * 触达策略
     */
    private String reachStrategy;

    /**
     * 0:推送决策,1:数据打标,2:合并数据推送决策
     */
    private Integer pushTarget;

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
     * 是否自动刷新 0-否，1-是
     */
    private Integer autoRefresh;

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

    public String getRuleNumber() {
        return ruleNumber;
    }

    public void setRuleNumber(String ruleNumber) {
        this.ruleNumber = ruleNumber == null ? null : ruleNumber.trim();
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName == null ? null : ruleName.trim();
    }

    public Long getDependencyTemplateId() {
        return dependencyTemplateId;
    }

    public void setDependencyTemplateId(Long dependencyTemplateId) {
        this.dependencyTemplateId = dependencyTemplateId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAutoTime() {
        return autoTime;
    }

    public void setAutoTime(String autoTime) {
        this.autoTime = autoTime == null ? null : autoTime.trim();
    }

    public String getPushDatasets() {
        return pushDatasets;
    }

    public void setPushDatasets(String pushDatasets) {
        this.pushDatasets = pushDatasets == null ? null : pushDatasets.trim();
    }

    public String getReachStrategy() {
        return reachStrategy;
    }

    public void setReachStrategy(String reachStrategy) {
        this.reachStrategy = reachStrategy == null ? null : reachStrategy.trim();
    }

    public Integer getPushTarget() {
        return pushTarget;
    }

    public void setPushTarget(Integer pushTarget) {
        this.pushTarget = pushTarget;
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

    public Integer getAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(Integer autoRefresh) {
        this.autoRefresh = autoRefresh;
    }
}