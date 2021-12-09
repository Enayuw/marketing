package com.br.marketing.entity;

import java.util.Date;

public class FastTaskRule {
    /**
     * 
     */
    private Long id;

    /**
     * 规则名称
     */
    private Long ruleName;

    /**
     * 规则编号
     */
    private Long ruleNumber;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 数据条件
     */
    private String dataCondition;

    /**
     * 策略编号
     */
    private String strategyId;

    /**
     * 产品信息
     */
    private String productInfo;

    /**
     * 产品输出字段
     */
    private String productField;

    /**
     * 客户回传字段
     */
    private String callbackInfo;

    /**
     * 跑分日期
     */
    private String taskTime;

    /**
     * 状态码1-开启；0-关闭
     */
    private Integer status;

    /**
     * 操作人id
     */
    private String optId;

    /**
     * 操作人姓名
     */
    private String optName;

    /**
     * 有效字段1-有效；9-无效
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

    public Long getRuleName() {
        return ruleName;
    }

    public void setRuleName(Long ruleName) {
        this.ruleName = ruleName;
    }

    public Long getRuleNumber() {
        return ruleNumber;
    }

    public void setRuleNumber(Long ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getDataCondition() {
        return dataCondition;
    }

    public void setDataCondition(String dataCondition) {
        this.dataCondition = dataCondition == null ? null : dataCondition.trim();
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId == null ? null : strategyId.trim();
    }

    public String getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(String productInfo) {
        this.productInfo = productInfo == null ? null : productInfo.trim();
    }

    public String getProductField() {
        return productField;
    }

    public void setProductField(String productField) {
        this.productField = productField == null ? null : productField.trim();
    }

    public String getCallbackInfo() {
        return callbackInfo;
    }

    public void setCallbackInfo(String callbackInfo) {
        this.callbackInfo = callbackInfo == null ? null : callbackInfo.trim();
    }

    public String getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(String taskTime) {
        this.taskTime = taskTime == null ? null : taskTime.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOptId() {
        return optId;
    }

    public void setOptId(String optId) {
        this.optId = optId == null ? null : optId.trim();
    }

    public String getOptName() {
        return optName;
    }

    public void setOptName(String optName) {
        this.optName = optName == null ? null : optName.trim();
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