package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;


public class PushInfoListVO {

    @ApiModelProperty(value = "任务流水号")
    private Long id;

    @ApiModelProperty(value = "apicode")
    private String mApiCode;

    @ApiModelProperty(value = "跑分批次号")
    private String mCusBatchNumberList;

    @ApiModelProperty(value = "规则条件")
    private String mRuleConditionShow;

    private String mRuleCondition;

    @ApiModelProperty(value = "推送数量")
    private Integer mRealyNum;

    @ApiModelProperty(value = "推送时间")
    private String createTime;

    @ApiModelProperty(value = "执行状态 1-执行中；2-执行成功；3-执行失败")
    private Integer mStatus;

    public String getmStatusDesc() {
        if (mStatus.equals(1)) {
            return "执行中";
        } else if (mStatus.equals(2)) {
            return "执行成功";
        } else {
            return "执行失败";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getmApiCode() {
        return mApiCode;
    }

    public void setmApiCode(String mApiCode) {
        this.mApiCode = mApiCode;
    }

    public String getmCusBatchNumberList() {
        return mCusBatchNumberList;
    }

    public void setmCusBatchNumberList(String mCusBatchNumberList) {
        this.mCusBatchNumberList = mCusBatchNumberList;
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
}
