package com.br.marketing.dto;

import com.br.marketing.entity.auth.MarketingUserDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;


public class PushCustomerDTO {

    @Schema(description = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @Schema(description = "上传开始时间")
//    @NotNull(message = "上传开始时间不能为空")
    private String uploadBeginTime;

    @Schema(description = "上传结束时间")
//    @NotNull(message = "上传结束时间不能为空")
    private String uploadEndTime;

    @Schema(description = "跑分执行开始时间")
//    @NotNull(message = "跑分执行开始时间不能为空")
    private String scoreBeginTime;

    @Schema(description = "跑分执行结束时间")
//    @NotNull(message = "跑分执行结束时间不能为空")
    private String scoreEndTime;

    @Schema(description = "批次号")
    //@NotNull(message = "批次号不能为空")
    //@NotEmpty(message = "批次号不能为空")
    //@Size(min = 1,message = "批次号不能为空")
    private List<String> batchNumberList;

    @Schema(description = "跑分记录id")
    //@NotNull(message = "fileIdList不能为空")
    //@NotEmpty(message = "fileIdList不能为空")
    //@Size(min = 1,message = "fileIdList不能为空")
    private List<Long> fileIdList;

    @Schema(description = "查询规则")
    private String mRuleCondition;

    @Schema(description = "评分分布规则")
    private String mScoreCondition;

    @Schema(description = "标签规则")
    private String mTagCondition;

    @Schema(description = "查询规则用于前端展示文本")
    private String mRuleConditionShow;

    @Schema(description = "推送数量")
    private Integer mPlanNum;

    @Schema(description = "预览推送数量")
    private Integer mPrePlanNum;

    @Schema(description = "百分比")
    private BigDecimal mPercentage;

    @Schema(description = "用户信息",hidden = true)
    private MarketingUserDetail userDetail;

    @Schema(description = "生成数据包名称")
    private String dataPackageName;

    @Schema(description = "数据集名称")
    private String batchName;

    @Schema(description = "规则模版名称")
    private String ruleModelName;

    @Schema(description = "标签字段名称")
    private String labelName;

    @Schema(description = "跑分是否合并")
    private Boolean isScoreMerge;

    @Schema(description = "合并跑分字段:custNum或cell")
    private String scoreMergeField;

    @Schema(description = "推送系统类型")
    private Integer pushTarget;

    @Schema(description = "任务类型 0：跑分任务，1：上传任务")
    private Integer taskType;

    @Schema(description = "上传记录id，多个用，分割")
    private String uploadReportId;

    @Schema(description = "重推框定数据时间")
    private String repushTime;

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

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getRuleModelName() {
        return ruleModelName;
    }

    public void setRuleModelName(String ruleModelName) {
        this.ruleModelName = ruleModelName;
    }

    public String getmScoreCondition() {
        return mScoreCondition;
    }

    public void setmScoreCondition(String mScoreCondition) {
        this.mScoreCondition = mScoreCondition;
    }

    public String getmTagCondition() {
        return mTagCondition;
    }

    public void setmTagCondition(String mTagCondition) {
        this.mTagCondition = mTagCondition;
    }


    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }


    public Boolean getIsScoreMerge() {
        return isScoreMerge;
    }

    public void setIsScoreMerge(Boolean isScoreMerge) {
        this.isScoreMerge = isScoreMerge;
    }

    public String getScoreMergeField() {
        return scoreMergeField;
    }

    public void setScoreMergeField(String scoreMergeField) {
        this.scoreMergeField = scoreMergeField;
    }

    public Boolean getScoreMerge() {
        return isScoreMerge;
    }

    public void setScoreMerge(Boolean scoreMerge) {
        isScoreMerge = scoreMerge;
    }

    public Integer getPushTarget() {
        return pushTarget;
    }

    public void setPushTarget(Integer pushTarget) {
        this.pushTarget = pushTarget;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public String getUploadReportId() {
        return uploadReportId;
    }

    public void setUploadReportId(String uploadReportId) {
        this.uploadReportId = uploadReportId;
    }

    public String getRepushTime() {
        return repushTime;
    }

    public void setRepushTime(String repushTime) {
        this.repushTime = repushTime;
    }

}