package com.br.marketing.entity;

import java.util.Date;

public class ScoreOptLog {
    /**
     * 
     */
    private Long id;

    /**
     * 跑分规则id
     */
    private String scoreRuleId;

    /**
     * 操作人id
     */
    private String optUserId;

    /**
     * 操作人姓名
     */
    private String optUserName;

    /**
     * 删除标志；1-正常；9-删除；
     */
    private Integer isDel;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 跑分时间 格式HH:mm
     */
    private String startTime;

    /**
     * 规则展示信息
     */
    private String conditionShowInfo;

    /**
     * 商户编号
     */
    private String cid;

    /**
     * 账户标识
     */
    private String apicode;

    /**
     * 策略产品展示信息
     */
    private String strategyProductShow;

    /**
     * 开启状态 1-开启；2-禁用；3-开启中
     */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScoreRuleId() {
        return scoreRuleId;
    }

    public void setScoreRuleId(String scoreRuleId) {
        this.scoreRuleId = scoreRuleId == null ? null : scoreRuleId.trim();
    }

    public String getOptUserId() {
        return optUserId;
    }

    public void setOptUserId(String optUserId) {
        this.optUserId = optUserId == null ? null : optUserId.trim();
    }

    public String getOptUserName() {
        return optUserName;
    }

    public void setOptUserName(String optUserName) {
        this.optUserName = optUserName == null ? null : optUserName.trim();
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

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName == null ? null : ruleName.trim();
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime == null ? null : startTime.trim();
    }

    public String getConditionShowInfo() {
        return conditionShowInfo;
    }

    public void setConditionShowInfo(String conditionShowInfo) {
        this.conditionShowInfo = conditionShowInfo == null ? null : conditionShowInfo.trim();
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getApicode() {
        return apicode;
    }

    public void setApicode(String apicode) {
        this.apicode = apicode == null ? null : apicode.trim();
    }

    public String getStrategyProductShow() {
        return strategyProductShow;
    }

    public void setStrategyProductShow(String strategyProductShow) {
        this.strategyProductShow = strategyProductShow == null ? null : strategyProductShow.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}