package com.br.marketing.entity;

import java.util.Date;

public class ScoreRuleConfig {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则简拼
     */
    private String ruleNameShort;

    /**
     * 跑分时间 格式HH:mm
     */
    private String startTime;

    /**
     * 策略产品配置信息
     */
    private String strategyProductJson;

    /**
     * 策略
     */
    private String strategyId;

    /**
     * 产品信息
     */
    private String productInfo;

    /**
     * 返回用户基本字段表头 字段用逗号分隔
     */
    private String baseInfo;

    /**
     * 规则类型 1-配置策略规则；2-配置产品规则；3-不需调用画像规则
     */
    private Integer ruleType;

    /**
     * 是否有效1-有效；9-无效；
     */
    private Integer isDel;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 任务执行策略 1-一次性全量；2-一次性验证；3-每个任务的周期；4-每日定时
     */
    private Integer execType;

    /**
     * 是否自动生成跑分任务 0-否；1-是
     */
    private Integer autoBuild;

    /**
     * 周期天数
     */
    private Integer cycleDay;

    /**
     * 
     */
    private String cycleEndDay;

    /**
     * 推送客户类型 0 文件，1 api 默认支持文件推送
     */
    private Integer pushType;

    /**
     * 跑分类型  0 有策略跑分 1 无策略不跑分 2 数据产品跑分
     */
    private Integer taskType;

    /**
     * 开启状态 1-开启；2-禁用；3-开启中
     */
    private Integer status;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 策略产品展示信息，后期有可能维护成需要配置的产品
     */
    private String strategyProductShow;

    /**
     * 3k返回的加密类型 0-不加密；1-MD5；2-sha256
     */
    private Integer threekEncryptType;

    /**
     * 是否线上跑分1-线上；2-离线
     */
    private Integer isOnline;

    /**
     * 是否叠加有效期数据 0-否，1-是
     */
    private Integer isStackValidity;

    /**
     * 跑分优先级 0最高，9最低
     */
    private Integer priority;

    /**
     * 规则校验状态，见 {@link com.br.marketing.common.enums.ScoreRuleCheckStatusEnum}
     */
    private Integer checkStatus;

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

    public String getRuleNameShort() {
        return ruleNameShort;
    }

    public void setRuleNameShort(String ruleNameShort) {
        this.ruleNameShort = ruleNameShort == null ? null : ruleNameShort.trim();
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime == null ? null : startTime.trim();
    }

    public String getStrategyProductJson() {
        return strategyProductJson;
    }

    public void setStrategyProductJson(String strategyProductJson) {
        this.strategyProductJson = strategyProductJson == null ? null : strategyProductJson.trim();
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

    public String getBaseInfo() {
        return baseInfo;
    }

    public void setBaseInfo(String baseInfo) {
        this.baseInfo = baseInfo == null ? null : baseInfo.trim();
    }

    public Integer getRuleType() {
        return ruleType;
    }

    public void setRuleType(Integer ruleType) {
        this.ruleType = ruleType;
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

    public Integer getExecType() {
        return execType;
    }

    public void setExecType(Integer execType) {
        this.execType = execType;
    }

    public Integer getAutoBuild() {
        return autoBuild;
    }

    public void setAutoBuild(Integer autoBuild) {
        this.autoBuild = autoBuild;
    }

    public Integer getCycleDay() {
        return cycleDay;
    }

    public void setCycleDay(Integer cycleDay) {
        this.cycleDay = cycleDay;
    }

    public String getCycleEndDay() {
        return cycleEndDay;
    }

    public void setCycleEndDay(String cycleEndDay) {
        this.cycleEndDay = cycleEndDay == null ? null : cycleEndDay.trim();
    }

    public Integer getPushType() {
        return pushType;
    }

    public void setPushType(Integer pushType) {
        this.pushType = pushType;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getStrategyProductShow() {
        return strategyProductShow;
    }

    public void setStrategyProductShow(String strategyProductShow) {
        this.strategyProductShow = strategyProductShow == null ? null : strategyProductShow.trim();
    }

    public Integer getThreekEncryptType() {
        return threekEncryptType;
    }

    public void setThreekEncryptType(Integer threekEncryptType) {
        this.threekEncryptType = threekEncryptType;
    }

    public Integer getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Integer isOnline) {
        this.isOnline = isOnline;
    }

    public Integer getIsStackValidity() {
        return isStackValidity;
    }

    public void setIsStackValidity(Integer isStackValidity) {
        this.isStackValidity = isStackValidity;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(Integer checkStatus) {
        this.checkStatus = checkStatus;
    }
}