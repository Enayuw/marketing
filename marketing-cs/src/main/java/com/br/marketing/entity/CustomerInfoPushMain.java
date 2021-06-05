package com.br.marketing.entity;

import java.util.Date;

public class CustomerInfoPushMain {
    /**
     * 主键，任务流水号
     */
    private Long id;

    /**
     * 商户编号
     */
    private String mApiCode;

    /**
     * 模型名称
     */
    private String mModel;

    /**
     * 模型版本
     */
    private String mModelVersion;

    /**
     * 推送数量最小值
     */
    private Integer mNumMin;

    /**
     * 推送数量最大值
     */
    private Integer mNumMax;

    /**
     * 分值最小数量
     */
    private Integer mScoreMin;

    /**
     * 分值最大数量
     */
    private Integer mScoreMax;

    /**
     * 计划推送数量
     */
    private Integer mPlanNum;

    /**
     * 实际推送数量
     */
    private Integer mRealyNum;

    /**
     * 执行状态 1-执行中；2-执行成功；3-执行失败
     */
    private Integer mStatus;

    /**
     * 逻辑删除 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 入库时间，推送时间
     */
    private Date createTime;

    /**
     * 更新记录时间
     */
    private Date updateTime;

    /**
     * 执行结束时间
     */
    private Date finishTime;

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
        this.mApiCode = mApiCode == null ? null : mApiCode.trim();
    }

    public String getmModel() {
        return mModel;
    }

    public void setmModel(String mModel) {
        this.mModel = mModel == null ? null : mModel.trim();
    }

    public String getmModelVersion() {
        return mModelVersion;
    }

    public void setmModelVersion(String mModelVersion) {
        this.mModelVersion = mModelVersion == null ? null : mModelVersion.trim();
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

    public Integer getmPlanNum() {
        return mPlanNum;
    }

    public void setmPlanNum(Integer mPlanNum) {
        this.mPlanNum = mPlanNum;
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

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }
}