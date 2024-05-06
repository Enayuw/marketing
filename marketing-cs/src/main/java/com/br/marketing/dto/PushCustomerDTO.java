package com.br.marketing.dto;

import com.br.marketing.entity.auth.MarketingUserDetail;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;


public class PushCustomerDTO {

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @ApiModelProperty(value = "上传开始时间")
//    @NotNull(message = "上传开始时间不能为空")
    private String uploadBeginTime;

    @ApiModelProperty(value = "上传结束时间")
//    @NotNull(message = "上传结束时间不能为空")
    private String uploadEndTime;

    @ApiModelProperty(value = "跑分执行开始时间")
//    @NotNull(message = "跑分执行开始时间不能为空")
    private String scoreBeginTime;

    @ApiModelProperty(value = "跑分执行结束时间")
//    @NotNull(message = "跑分执行结束时间不能为空")
    private String scoreEndTime;

    @ApiModelProperty(value = "批次号")
    @NotNull(message = "批次号不能为空")
    @NotEmpty(message = "批次号不能为空")
    @Size(min = 1,message = "批次号不能为空")
    private List<String> batchNumberList;

    @ApiModelProperty(value = "跑分记录id")
    @NotNull(message = "fileIdList不能为空")
    @NotEmpty(message = "fileIdList不能为空")
    @Size(min = 1,message = "fileIdList不能为空")
    private List<Long> fileIdList;

    @ApiModelProperty(value = "查询规则")
    private String mRuleCondition;

    @ApiModelProperty(value = "查询规则用于前端展示文本")
    private String mRuleConditionShow;

    @ApiModelProperty(value = "推送数量")
    private Integer mPlanNum;

    @ApiModelProperty(value = "预览推送数量")
    private Integer mPrePlanNum;

    @ApiModelProperty(value = "百分比")
    private BigDecimal mPercentage;

    @ApiModelProperty(value = "用户信息",hidden = true)
    private MarketingUserDetail userDetail;

    @ApiModelProperty(value = "生成数据包名称")
    private String dataPackageName;

    public Integer getmPrePlanNum() {
        return mPrePlanNum;
    }

    public void setmPrePlanNum(Integer mPrePlanNum) {
        this.mPrePlanNum = mPrePlanNum;
    }

    public String getDataPackageName() {
        return dataPackageName;
    }
    public void setDataPackageName(String dataPackageName) {
        this.dataPackageName = dataPackageName;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getUploadBeginTime() {
        return uploadBeginTime;
    }

    public void setUploadBeginTime(String uploadBeginTime) {
        this.uploadBeginTime = uploadBeginTime;
    }

    public String getUploadEndTime() {
        return uploadEndTime;
    }

    public void setUploadEndTime(String uploadEndTime) {
        this.uploadEndTime = uploadEndTime;
    }

    public String getScoreBeginTime() {
        return scoreBeginTime;
    }

    public void setScoreBeginTime(String scoreBeginTime) {
        this.scoreBeginTime = scoreBeginTime;
    }

    public String getScoreEndTime() {
        return scoreEndTime;
    }

    public void setScoreEndTime(String scoreEndTime) {
        this.scoreEndTime = scoreEndTime;
    }

    public List<String> getBatchNumberList() {
        return batchNumberList;
    }

    public void setBatchNumberList(List<String> batchNumberList) {
        this.batchNumberList = batchNumberList;
    }

    public List<Long> getFileIdList() {
        return fileIdList;
    }

    public void setFileIdList(List<Long> fileIdList) {
        this.fileIdList = fileIdList;
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

    public MarketingUserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(MarketingUserDetail userDetail) {
        this.userDetail = userDetail;
    }
}
