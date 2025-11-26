package com.br.marketing.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * b_customer_info_push_main
 * @author 
 */
public class CustomerInfoPushMain implements Serializable {
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
     * 客户批次号冗余字段
     */
    private String mCusBatchNumberList;

    /**
     * 执行状态 1-执行中;2-待确认;3-推送失败;4-确认成功;5-确认失败
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

    /**
     * 查询条件
     */
    private String mRuleCondition;

    /**
     * 查询条件前台展示
     */
    private String mRuleConditionShow;

    /**
     * 评分分布规则
     */
    private String mScoreCondition;

    /**
     * 百分比
     */
    private BigDecimal mPercentage;

    /**
     * 操作人id
     */
    private String optUserId;

    /**
     * 操作人姓名
     */
    private String optUserName;

    /**
     * 任务类型 0 跑分数据推决策, 1 跑分及撞库结果筛选推决策
     */
    private Integer filterType;

    /**
     * 数据集名称
     */
    private String batchName;

    private String extend;

    /**
     * 数据源类型：1-跑分;2-转化
     */
    private Integer sourceType;

    /**
     * 任务生成类型1-手动生成；2-自动生成
     */
    private Integer buildType;

    /**
     * 触达策略编号
     */
    private String strategyCode;

    /**
     * 标签规则
     */
    private String tagContent;

    /**
     * 0:推送决策,1:数据打标,2:合并数据推送决策
     */
    private Integer pushTarget;

    /**
     * 标签名称
     */
    private String labelName;

    /**
     * 上传数据记录id:多个,分割
     */
    private String uploadReportIds;

    private static final long serialVersionUID = 1L;

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

    public String getmCusBatchNumberList() {
        return mCusBatchNumberList;
    }

    public void setmCusBatchNumberList(String mCusBatchNumberList) {
        this.mCusBatchNumberList = mCusBatchNumberList == null ? null : mCusBatchNumberList.trim();
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

    public String getmRuleCondition() {
        return mRuleCondition;
    }

    public void setmRuleCondition(String mRuleCondition) {
        this.mRuleCondition = mRuleCondition == null ? null : mRuleCondition.trim();
    }

    public String getmRuleConditionShow() {
        return mRuleConditionShow;
    }

    public void setmRuleConditionShow(String mRuleConditionShow) {
        this.mRuleConditionShow = mRuleConditionShow == null ? null : mRuleConditionShow.trim();
    }

    public String getmScoreCondition() {
        return mScoreCondition;
    }

    public void setmScoreCondition(String mScoreCondition) {
        this.mScoreCondition = mScoreCondition == null ? null : mScoreCondition.trim();
    }

    public BigDecimal getmPercentage() {
        return mPercentage;
    }

    public void setmPercentage(BigDecimal mPercentage) {
        this.mPercentage = mPercentage;
    }

    public String getOptUserId() {
        return optUserId;
    }

    public void setOptUserId(String optUserId) {
        this.optUserId = optUserId == null ? null : optUserId.trim();
    }

    public String getOptUserName() {
        return optUserName;
    }

    public void setOptUserName(String optUserName) {
        this.optUserName = optUserName == null ? null : optUserName.trim();
    }

    public Integer getFilterType() {
        return filterType;
    }

    public void setFilterType(Integer filterType) {
        this.filterType = filterType;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName == null ? null : batchName.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getBuildType() {
        return buildType;
    }

    public void setBuildType(Integer buildType) {
        this.buildType = buildType;
    }

    public String getStrategyCode() {
        return strategyCode;
    }

    public void setStrategyCode(String strategyCode) {
        this.strategyCode = strategyCode == null ? null : strategyCode.trim();
    }

    public String getTagContent() {
        return tagContent;
    }

    public void setTagContent(String tagContent) {
        this.tagContent = tagContent == null ? null : tagContent.trim();
    }

    public Integer getPushTarget() {
        return pushTarget;
    }

    public void setPushTarget(Integer pushTarget) {
        this.pushTarget = pushTarget;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public String getUploadReportIds() {
        return uploadReportIds;
    }

    public void setUploadReportIds(String uploadReportIds) {
        this.uploadReportIds = uploadReportIds;
    }
}