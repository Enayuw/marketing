package com.br.marketing.vo;

import com.br.marketing.enums.PushRuleStatusEnum;
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

    @ApiModelProperty(value = "评分分布规则用于前端展示文本")
    private String mScoreConditionShow;

    private String mScoreCondition;

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
        if (mStatus.equals(PushRuleStatusEnum.RUNNING.getValue())) {
            return PushRuleStatusEnum.RUNNING.getDesc();
        } else if (mStatus.equals(PushRuleStatusEnum.TO_BE_CONFIRMED.getValue())) {
            return PushRuleStatusEnum.TO_BE_CONFIRMED.getDesc();
        } else if (mStatus.equals(PushRuleStatusEnum.PUSH_FAIL.getValue())) {
            return PushRuleStatusEnum.PUSH_FAIL.getDesc();
        } else if (mStatus.equals(PushRuleStatusEnum.CONFIRMED_SUCCESS.getValue())) {
            return PushRuleStatusEnum.CONFIRMED_SUCCESS.getDesc();
        } else if (mStatus.equals(PushRuleStatusEnum.CONFIRMED_FAIL.getValue())) {
            return PushRuleStatusEnum.CONFIRMED_FAIL.getDesc();
        } else if (mStatus.equals(PushRuleStatusEnum.TO_BE_RUNNING.getValue())) {
            return PushRuleStatusEnum.TO_BE_RUNNING.getDesc();
        } else if (mStatus.equals(PushRuleStatusEnum.CONFIRMED_TIME_OUT.getValue())) {
            // 产品规定展示这一类别
            return PushRuleStatusEnum.TO_BE_CONFIRMED.getDesc();
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

    public String getmScoreConditionShow() {
        return mScoreConditionShow;
    }

    public void setmScoreConditionShow(String mScoreConditionShow) {
        this.mScoreConditionShow = mScoreConditionShow;
    }

    public String getmScoreCondition() {
        return mScoreCondition;
    }

    public void setmScoreCondition(String mScoreCondition) {
        this.mScoreCondition = mScoreCondition;
    }
}
