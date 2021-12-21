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
    private String ruleName;

    /**
     * 规则编号
     */
    private String ruleNumber;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 数据条件
     */
    private String dataCondition;

    /**
     * 1-全量；2-未跑分
     */
    private Integer dataType;

    /**
     * 所选数据的id 逗号分隔
     */
    private String dataIdDesc;

    /**
     * 规则id
     */
    private Long ruleId;

    /**
     * 跑分类型 0 正常跑分 1 不跑分 2 产品配置
     */
    private Integer taskType;

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

    /**
     * 未跑分数据量
     */
    private Integer untaskNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName == null ? null : ruleName.trim();
    }

    public String getRuleNumber() {
        return ruleNumber;
    }

    public void setRuleNumber(String ruleNumber) {
        this.ruleNumber = ruleNumber == null ? null : ruleNumber.trim();
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

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getDataIdDesc() {
        return dataIdDesc;
    }

    public void setDataIdDesc(String dataIdDesc) {
        this.dataIdDesc = dataIdDesc == null ? null : dataIdDesc.trim();
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
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

    public Integer getUntaskNum() {
        return untaskNum;
    }

    public void setUntaskNum(Integer untaskNum) {
        this.untaskNum = untaskNum;
    }
}