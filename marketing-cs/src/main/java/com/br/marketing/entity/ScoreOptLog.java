package com.br.marketing.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@ApiModel(value = "跑分配置变更记录", description = "跑分配置变更记录")
public class ScoreOptLog {
    /**
     *
     */
    @ApiModelProperty(value = "主键", dataType = "long", position = 0)
    private Long id;

    /**
     * 跑分规则id
     */
    @ApiModelProperty(value = "跑分规则id", dataType = "String", position = 1)
    private String scoreRuleId;

    /**
     * 操作人id
     */
    @ApiModelProperty(value = "操作人id", dataType = "String", position = 2)
    private String optUserId;

    /**
     * 操作人姓名
     */
    @ApiModelProperty(value = "操作人姓名", dataType = "String", position = 3)
    private String optUserName;

    /**
     * 删除标志；1-正常；9-删除；
     */
    @ApiModelProperty(value = "删除标志；1-正常；9-删除；", dataType = "Integer", position = 4, hidden = true)
    private Integer isDel;

    /**
     * 入库时间
     */
    @ApiModelProperty(value = "入库时间", dataType = "Date", position = 5)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间", dataType = "Date", position = 6, hidden = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称", dataType = "String", position = 7)
    private String ruleName;

    /**
     * 跑分时间 格式HH:mm
     */
    @ApiModelProperty(value = "跑分时间 格式HH:mm", dataType = "String", position = 8)
    private String startTime;

    /**
     * 规则展示信息
     */
    @ApiModelProperty(value = "规则展示信息", dataType = "String", position = 9)
    private String conditionShowInfo;

    /**
     * 商户编号
     */
    @ApiModelProperty(value = "商户编号", dataType = "String", position = 10)
    private String cid;

    /**
     * 账户标识
     */
    @ApiModelProperty(value = "账户标识", dataType = "String", position = 11)
    private String apicode;

    /**
     * 策略产品展示信息
     */
    @ApiModelProperty(value = "策略产品展示信息", dataType = "String", position = 12)
    private String strategyProductShow;

    /**
     * 开启状态 1-开启；2-禁用；3-开启中
     */
    @ApiModelProperty(value = "开启状态 1-开启；2-禁用；3-开启中", dataType = "Integer", position = 13)
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