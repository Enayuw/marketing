package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;


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

    @ApiModelProperty(value = "数据源类型")
    private Integer sourceType;

    @ApiModelProperty(value = "数据源内容")
    private String sourceCondition;

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

    public String getSourceCondition() {
        return sourceCondition;
    }

    public void setSourceCondition(String sourceCondition) {
        this.sourceCondition = sourceCondition;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }
}
