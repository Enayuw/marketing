package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;


public class ConditionSaveDTO{

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @ApiModelProperty(value = "规则名称")
    @NotNull(message = "规则名称不能为空")
    private String name;

    @ApiModelProperty(value = "查询规则")
    @NotNull(message = "查询规则不能为空")
    private String mRuleCondition;

    @ApiModelProperty(value = "查询规则用于前端展示文本")
    private String mRuleConditionShow;

    @ApiModelProperty(value = "评分分布规则")
    private String mScoreCondition;

    @ApiModelProperty(value = "评分分布规则用于前端展示文本")
    private String mScoreConditionShow;

    @ApiModelProperty(value = "数据源类型")
    private Integer sourceType;

    @ApiModelProperty(value = "数据源内容")
    private List<String> sources;

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getmRuleCondition() {
        return mRuleCondition;
    }

    public void setmRuleCondition(String mRuleCondition) {
        this.mRuleCondition = mRuleCondition;
    }

    public String getmRuleConditionShow() {
        return mRuleConditionShow;
    }

    public void setmRuleConditionShow(String mRuleConditionShow) {
        this.mRuleConditionShow = mRuleConditionShow;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getmScoreCondition() {
        return mScoreCondition;
    }

    public void setmScoreCondition(String mScoreCondition) {
        this.mScoreCondition = mScoreCondition;
    }

    public String getmScoreConditionShow() {
        return mScoreConditionShow;
    }

    public void setmScoreConditionShow(String mScoreConditionShow) {
        this.mScoreConditionShow = mScoreConditionShow;
    }
}
