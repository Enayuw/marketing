package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public class PushInfoDetailVO {

    @Schema(description = "任务流水号")
    private Long id;

    @Schema(description = "推送时间")
    private Date createTime;

    @Schema(description = "内部批次号")
    private String mBatchNumber;

    @Schema(description = "客户批次号")
    private String mCusBatchNumber;

    @Schema(description = "模型名称")
    private String mModel;

    @Schema(description = "模型版本")
    private String mModelVersion;

    @Schema(description = "top最小值")
    private Integer mNumMin;

    @Schema(description = "top最大值")
    private Integer mNumMax;

    @Schema(description = "最小分值")
    private Integer mScoreMin;

    @Schema(description = "最大分值")
    private Integer mScoreMax;

    @Schema(description = "推送数量")
    private Integer mRealyNum;

    @Schema(description = "执行状态 1-执行中；2-执行成功；3-执行失败")
    private Integer mStatus;

    @Schema(description = "执行状态文本描述")
    private String mStatusDesc;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getmBatchNumber() {
        return mBatchNumber;
    }

    public void setmBatchNumber(String mBatchNumber) {
        this.mBatchNumber = mBatchNumber;
    }

    public String getmModel() {
        return mModel;
    }

    public void setmModel(String mModel) {
        this.mModel = mModel;
    }

    public String getmModelVersion() {
        return mModelVersion;
    }

    public void setmModelVersion(String mModelVersion) {
        this.mModelVersion = mModelVersion;
    }

    public Integer getmNumMin() {
        return mNumMin;
    }

    public void setmNumMin(Integer mNumMin) {
        this.mNumMin = mNumMin;
    }

    public Integer getmNumMax() {
        return mNumMax;
    }

    public void setmNumMax(Integer mNumMax) {
        this.mNumMax = mNumMax;
    }

    public Integer getmScoreMin() {
        return mScoreMin;
    }

    public void setmScoreMin(Integer mScoreMin) {
        this.mScoreMin = mScoreMin;
    }

    public Integer getmScoreMax() {
        return mScoreMax;
    }

    public void setmScoreMax(Integer mScoreMax) {
        this.mScoreMax = mScoreMax;
    }

    public Integer getmRealyNum() {
        return mRealyNum;
    }

    public void setmRealyNum(Integer mRealyNum) {
        this.mRealyNum = mRealyNum;
    }

    public Integer getmStatus() {
        return mStatus;
    }

    public void setmStatus(Integer mStatus) {
        this.mStatus = mStatus;
    }

    public String getmCustBatchNumber() {
        return mCusBatchNumber;
    }

    public void setmCustBatchNumber(String mCustBatchNumber) {
        this.mCusBatchNumber = mCustBatchNumber;
    }
}
