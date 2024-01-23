package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public class PushInfoListVO {

    @ApiModelProperty(value = "任务流水号")
    private Long id;

    @ApiModelProperty(value = "apicode")
    private String apiCode;

    @ApiModelProperty(value = "跑分批次号")
    private String batchNumbers;

    private String mCusBatchNumberList;

    @ApiModelProperty(value = "规则条件")
    private String mRuleConditionShow;

    private String mRuleCondition;

    @ApiModelProperty(value = "计划推送数量")
    private Integer mPlanNum;

    @ApiModelProperty(value = "百分比")
    private BigDecimal mPercentage;

    @ApiModelProperty(value = "实际推送数量")
    private Integer mRealyNum;

    @ApiModelProperty(value = "推送时间")
    private String createTime;

    @ApiModelProperty(value = "执行状态 1-执行中;2-待确认;3-推送失败;4-确认成功;5-确认失败")
    private Integer mStatus;

    @ApiModelProperty(value = "推送结果返回")
    private List<Map> returnMessages;

    @ApiModelProperty(value = "场景")
    private String userType;

    public String getmStatusDesc() {
        if (mStatus.equals(1)) {
            return "执行中";
        } else if (mStatus.equals(2)) {
            return "待确认";
        } else if (mStatus.equals(3)) {
            return "推送失败";
        } else if (mStatus.equals(4)) {
            return "确认成功";
        } else if (mStatus.equals(5)) {
            return "确认失败";
        } else {
            return "状态异常";
        }
    }

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
        this.apiCode = apiCode;
    }

    public String getmCusBatchNumberList() {
        return mCusBatchNumberList;
    }

    public void setmCusBatchNumberList(String mCusBatchNumberList) {
        this.mCusBatchNumberList = mCusBatchNumberList;
    }

    public String getBatchNumbers() {
        return batchNumbers;
    }

    public void setBatchNumbers(String batchNumbers) {
        this.batchNumbers = batchNumbers;
    }

    public String getmRuleConditionShow() {
        return mRuleConditionShow;
    }

    public void setmRuleConditionShow(String mRuleConditionShow) {
        this.mRuleConditionShow = mRuleConditionShow;
    }

    public String getmRuleCondition() {
        return mRuleCondition;
    }

    public void setmRuleCondition(String mRuleCondition) {
        this.mRuleCondition = mRuleCondition;
    }

    public Integer getmRealyNum() {
        return mRealyNum;
    }

    public void setmRealyNum(Integer mRealyNum) {
        this.mRealyNum = mRealyNum;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getmStatus() {
        return mStatus;
    }

    public void setmStatus(Integer mStatus) {
        this.mStatus = mStatus;
    }

    public Integer getmPlanNum() {
        return mPlanNum;
    }

    public void setmPlanNum(Integer mPlanNum) {
        this.mPlanNum = mPlanNum;
    }

    public BigDecimal getmPercentage() {
        return mPercentage;
    }

    public void setmPercentage(BigDecimal mPercentage) {
        this.mPercentage = mPercentage;
    }

    public List<Map> getReturnMessages() {
        return returnMessages;
    }

    public void setReturnMessages(List<Map> returnMessages) {
        this.returnMessages = returnMessages;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
