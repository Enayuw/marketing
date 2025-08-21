package com.br.marketing.entity;

import java.util.Date;

public class MarketingTaskExtend {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private String apiCode;

    /**
     * b_marketing_task表id
     */
    private Long taskId;

    /**
     *
     */
    private String cusTaskId;

    /**
     * 规则id
     */
    private Long ruleId;

    /**
     * 场景
     */
    private String groupType;

    /**
     * 1-有效；9-删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 上传时间
     */
    private String uploadTime;

    /**
     * 扩展表头字段
     */
    private String extendShowTitle;

    /**
     * 策略产品变量信息
     */
    private String strategyProductJson;

    /**
     * 数据条件
     */
    private String dataCondition;

    /**
     * 数据条件类型1-自动；2-手动
     */
    private Integer conditionType;

    /**
     * 数据范围展示
     */
    private String conditionInfoShow;

    /**
     * 扩展信息字段
     */
    private String extendConfigInfo;

    /**
     * 标签名称
     */
    private String labelName;

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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getCusTaskId() {
        return cusTaskId;
    }

    public void setCusTaskId(String cusTaskId) {
        this.cusTaskId = cusTaskId == null ? null : cusTaskId.trim();
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType == null ? null : groupType.trim();
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

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime == null ? null : uploadTime.trim();
    }

    public String getExtendShowTitle() {
        return extendShowTitle;
    }

    public void setExtendShowTitle(String extendShowTitle) {
        this.extendShowTitle = extendShowTitle == null ? null : extendShowTitle.trim();
    }

    public String getStrategyProductJson() {
        return strategyProductJson;
    }

    public void setStrategyProductJson(String strategyProductJson) {
        this.strategyProductJson = strategyProductJson == null ? null : strategyProductJson.trim();
    }

    public String getDataCondition() {
        return dataCondition;
    }

    public void setDataCondition(String dataCondition) {
        this.dataCondition = dataCondition == null ? null : dataCondition.trim();
    }

    public Integer getConditionType() {
        return conditionType;
    }

    public void setConditionType(Integer conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionInfoShow() {
        return conditionInfoShow;
    }

    public void setConditionInfoShow(String conditionInfoShow) {
        this.conditionInfoShow = conditionInfoShow == null ? null : conditionInfoShow.trim();
    }

    public String getExtendConfigInfo() {
        return extendConfigInfo;
    }

    public void setExtendConfigInfo(String extendConfigInfo) {
        this.extendConfigInfo = extendConfigInfo == null ? null : extendConfigInfo.trim();
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName == null ? null : labelName.trim();
    }
}