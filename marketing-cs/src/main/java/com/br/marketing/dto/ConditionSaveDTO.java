package com.br.marketing.dto;

import com.br.marketing.entity.auth.MarketingUserDetail;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;


public class ConditionSaveDTO {

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @ApiModelProperty(value = "查询规则")
    @NotNull(message = "查询规则不能为空")
    private String mRuleCondition;

    @ApiModelProperty(value = "查询规则用于前端展示文本")
    private String mRuleConditionShow;

    @ApiModelProperty(value = "用户信息", hidden = true)
    private MarketingUserDetail userDetail;

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
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

    public MarketingUserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(MarketingUserDetail userDetail) {
        this.userDetail = userDetail;
    }
}
