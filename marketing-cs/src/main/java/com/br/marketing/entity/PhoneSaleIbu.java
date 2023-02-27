package com.br.marketing.entity;

import java.util.Date;

public class PhoneSaleIbu {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private String localId;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer mStatus;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 
     */
    private String uid;

    /**
     * 
     */
    private String userType;

    /**
     * 
     */
    private String purpose;

    /**
     * 
     */
    private String userCode;

    /**
     * 
     */
    private String userName;

    /**
     * 
     */
    private String gender;

    /**
     * 
     */
    private String phone;

    /**
     * 
     */
    private String signInTimeStr;

    /**
     * 
     */
    private String clickProductName;

    /**
     * 
     */
    private String clickTimeStr;

    /**
     * 
     */
    private String recommendList;

    /**
     * 
     */
    private String recommendH5List;

    /**
     * 
     */
    private String basicInfo;

    /**
     * 
     */
    private String realName;

    /**
     * 
     */
    private String supplement;

    /**
     * 
     */
    private String contract;

    /**
     * 
     */
    private String operator;

    /**
     * 
     */
    private String loanProductName;

    /**
     * 
     */
    private String loanTimeStr;

    /**
     * 
     */
    private String createTimeStr;

    /**
     * 
     */
    private String diffAmount;

    /**
     * 
     */
    private String faceRecognition;

    /**
     * 
     */
    private String firstApproveResult;

    /**
     * 
     */
    private String firstApproveTimeStr;

    /**
     * 
     */
    private String hasBindCard;

    /**
     * 
     */
    private String hasEverBorrow;

    /**
     * 
     */
    private String hasWithdraw;

    /**
     * 
     */
    private String insteadCommitFlag;

    /**
     * 
     */
    private String insteadCommitPname;

    /**
     * 
     */
    private String isTimely;

    /**
     * 
     */
    private String loanFailedTimeStr;

    /**
     * 
     */
    private String loanSuccessTimeStr;

    /**
     * 
     */
    private String loanWillingness;

    /**
     * 
     */
    private String aCardScore;

    /**
     * 
     */
    private String bucketName;

    /**
     * 
     */
    private String overdueDays;

    /**
     * 
     */
    private String prepayAmount;

    /**
     * 
     */
    private String prepayPname;

    /**
     * 
     */
    private String prepayTimeStr;

    /**
     * 
     */
    private String repayPname;

    /**
     * 
     */
    private String repayAmount;

    /**
     * 
     */
    private String repayTimeStr;

    /**
     * 
     */
    private String secondApproveResult;

    /**
     * 
     */
    private String secondApproveTimeStr;

    /**
     * 
     */
    private String applyAmount;

    /**
     * 
     */
    private String approveAmount;

    /**
     * 
     */
    private String source;

    /**
     * 
     */
    private String prodType;

    /**
     * 
     */
    private String score;

    /**
     * 
     */
    private String callTimes;

    /**
     * 
     */
    private String callAccessScore;

    /**
     * 
     */
    private String remark;

    /**
     * 
     */
    private String grade;

    /**
     * 
     */
    private String totalAmount;

    /**
     * 
     */
    private String surplusAmount;

    /**
     * 
     */
    private String pid;

    /**
     * 
     */
    private String pchannel;

    /**
     * 
     */
    private String channelName;

    /**
     * 
     */
    private String marketPurpose;

    /**
     * 
     */
    private String riskControlLabel;

    /**
     * 
     */
    private String firstLoginTimeStr;

    /**
     * 
     */
    private String planId;

    /**
     * 
     */
    private String goalsApp;

    /**
     * 
     */
    private String flowSideName;

    /**
     * 
     */
    private String flowSidePath;

    /**
     * 
     */
    private String cusTag;

    /**
     * 
     */
    private String abgroupPushOffsetStr;

    /**
     * 
     */
    private String extra1;

    /**
     * 
     */
    private String extra2;

    /**
     * 
     */
    private String extra3;

    /**
     * 
     */
    private String reserveField1;

    /**
     * 
     */
    private String creditTimeStr;

    /**
     * 
     */
    private String creditChannel;

    /**
     * 
     */
    private String amountStatus;

    /**
     * 
     */
    private String connectTimes;

    /**
     * 
     */
    private String zyApplyFlag;

    /**
     * 
     */
    private String zyApplySuccessFlag;

    /**
     * 
     */
    private String zyAmountStatus;

    /**
     * 
     */
    private String zyTotalUsableAmount;

    /**
     * 
     */
    private String isIdnumber;

    /**
     * 
     */
    private String isTaobao;

    /**
     * 
     */
    private String isNuclearapproval;

    /**
     * 
     */
    private String callaccessscore;

    /**
     * 
     */
    private String marketingScore;

    /**
     * 
     */
    private String noWithdrawOrders;

    /**
     * 
     */
    private String planData;

    /**
     * 
     */
    private String priorityScore;

    /**
     * 
     */
    private String callType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

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
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId == null ? null : localId.trim();
    }

    public Integer getmStatus() {
        return mStatus;
    }

    public void setmStatus(Integer mStatus) {
        this.mStatus = mStatus;
    }

    public String getDataMessage() {
        return dataMessage;
    }

    public void setDataMessage(String dataMessage) {
        this.dataMessage = dataMessage == null ? null : dataMessage.trim();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose == null ? null : purpose.trim();
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode == null ? null : userCode.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender == null ? null : gender.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getSignInTimeStr() {
        return signInTimeStr;
    }

    public void setSignInTimeStr(String signInTimeStr) {
        this.signInTimeStr = signInTimeStr == null ? null : signInTimeStr.trim();
    }

    public String getClickProductName() {
        return clickProductName;
    }

    public void setClickProductName(String clickProductName) {
        this.clickProductName = clickProductName == null ? null : clickProductName.trim();
    }

    public String getClickTimeStr() {
        return clickTimeStr;
    }

    public void setClickTimeStr(String clickTimeStr) {
        this.clickTimeStr = clickTimeStr == null ? null : clickTimeStr.trim();
    }

    public String getRecommendList() {
        return recommendList;
    }

    public void setRecommendList(String recommendList) {
        this.recommendList = recommendList == null ? null : recommendList.trim();
    }

    public String getRecommendH5List() {
        return recommendH5List;
    }

    public void setRecommendH5List(String recommendH5List) {
        this.recommendH5List = recommendH5List == null ? null : recommendH5List.trim();
    }

    public String getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(String basicInfo) {
        this.basicInfo = basicInfo == null ? null : basicInfo.trim();
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName == null ? null : realName.trim();
    }

    public String getSupplement() {
        return supplement;
    }

    public void setSupplement(String supplement) {
        this.supplement = supplement == null ? null : supplement.trim();
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract == null ? null : contract.trim();
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator == null ? null : operator.trim();
    }

    public String getLoanProductName() {
        return loanProductName;
    }

    public void setLoanProductName(String loanProductName) {
        this.loanProductName = loanProductName == null ? null : loanProductName.trim();
    }

    public String getLoanTimeStr() {
        return loanTimeStr;
    }

    public void setLoanTimeStr(String loanTimeStr) {
        this.loanTimeStr = loanTimeStr == null ? null : loanTimeStr.trim();
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr == null ? null : createTimeStr.trim();
    }

    public String getDiffAmount() {
        return diffAmount;
    }

    public void setDiffAmount(String diffAmount) {
        this.diffAmount = diffAmount == null ? null : diffAmount.trim();
    }

    public String getFaceRecognition() {
        return faceRecognition;
    }

    public void setFaceRecognition(String faceRecognition) {
        this.faceRecognition = faceRecognition == null ? null : faceRecognition.trim();
    }

    public String getFirstApproveResult() {
        return firstApproveResult;
    }

    public void setFirstApproveResult(String firstApproveResult) {
        this.firstApproveResult = firstApproveResult == null ? null : firstApproveResult.trim();
    }

    public String getFirstApproveTimeStr() {
        return firstApproveTimeStr;
    }

    public void setFirstApproveTimeStr(String firstApproveTimeStr) {
        this.firstApproveTimeStr = firstApproveTimeStr == null ? null : firstApproveTimeStr.trim();
    }

    public String getHasBindCard() {
        return hasBindCard;
    }

    public void setHasBindCard(String hasBindCard) {
        this.hasBindCard = hasBindCard == null ? null : hasBindCard.trim();
    }

    public String getHasEverBorrow() {
        return hasEverBorrow;
    }

    public void setHasEverBorrow(String hasEverBorrow) {
        this.hasEverBorrow = hasEverBorrow == null ? null : hasEverBorrow.trim();
    }

    public String getHasWithdraw() {
        return hasWithdraw;
    }

    public void setHasWithdraw(String hasWithdraw) {
        this.hasWithdraw = hasWithdraw == null ? null : hasWithdraw.trim();
    }

    public String getInsteadCommitFlag() {
        return insteadCommitFlag;
    }

    public void setInsteadCommitFlag(String insteadCommitFlag) {
        this.insteadCommitFlag = insteadCommitFlag == null ? null : insteadCommitFlag.trim();
    }

    public String getInsteadCommitPname() {
        return insteadCommitPname;
    }

    public void setInsteadCommitPname(String insteadCommitPname) {
        this.insteadCommitPname = insteadCommitPname == null ? null : insteadCommitPname.trim();
    }

    public String getIsTimely() {
        return isTimely;
    }

    public void setIsTimely(String isTimely) {
        this.isTimely = isTimely == null ? null : isTimely.trim();
    }

    public String getLoanFailedTimeStr() {
        return loanFailedTimeStr;
    }

    public void setLoanFailedTimeStr(String loanFailedTimeStr) {
        this.loanFailedTimeStr = loanFailedTimeStr == null ? null : loanFailedTimeStr.trim();
    }

    public String getLoanSuccessTimeStr() {
        return loanSuccessTimeStr;
    }

    public void setLoanSuccessTimeStr(String loanSuccessTimeStr) {
        this.loanSuccessTimeStr = loanSuccessTimeStr == null ? null : loanSuccessTimeStr.trim();
    }

    public String getLoanWillingness() {
        return loanWillingness;
    }

    public void setLoanWillingness(String loanWillingness) {
        this.loanWillingness = loanWillingness == null ? null : loanWillingness.trim();
    }

    public String getaCardScore() {
        return aCardScore;
    }

    public void setaCardScore(String aCardScore) {
        this.aCardScore = aCardScore == null ? null : aCardScore.trim();
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName == null ? null : bucketName.trim();
    }

    public String getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(String overdueDays) {
        this.overdueDays = overdueDays == null ? null : overdueDays.trim();
    }

    public String getPrepayAmount() {
        return prepayAmount;
    }

    public void setPrepayAmount(String prepayAmount) {
        this.prepayAmount = prepayAmount == null ? null : prepayAmount.trim();
    }

    public String getPrepayPname() {
        return prepayPname;
    }

    public void setPrepayPname(String prepayPname) {
        this.prepayPname = prepayPname == null ? null : prepayPname.trim();
    }

    public String getPrepayTimeStr() {
        return prepayTimeStr;
    }

    public void setPrepayTimeStr(String prepayTimeStr) {
        this.prepayTimeStr = prepayTimeStr == null ? null : prepayTimeStr.trim();
    }

    public String getRepayPname() {
        return repayPname;
    }

    public void setRepayPname(String repayPname) {
        this.repayPname = repayPname == null ? null : repayPname.trim();
    }

    public String getRepayAmount() {
        return repayAmount;
    }

    public void setRepayAmount(String repayAmount) {
        this.repayAmount = repayAmount == null ? null : repayAmount.trim();
    }

    public String getRepayTimeStr() {
        return repayTimeStr;
    }

    public void setRepayTimeStr(String repayTimeStr) {
        this.repayTimeStr = repayTimeStr == null ? null : repayTimeStr.trim();
    }

    public String getSecondApproveResult() {
        return secondApproveResult;
    }

    public void setSecondApproveResult(String secondApproveResult) {
        this.secondApproveResult = secondApproveResult == null ? null : secondApproveResult.trim();
    }

    public String getSecondApproveTimeStr() {
        return secondApproveTimeStr;
    }

    public void setSecondApproveTimeStr(String secondApproveTimeStr) {
        this.secondApproveTimeStr = secondApproveTimeStr == null ? null : secondApproveTimeStr.trim();
    }

    public String getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(String applyAmount) {
        this.applyAmount = applyAmount == null ? null : applyAmount.trim();
    }

    public String getApproveAmount() {
        return approveAmount;
    }

    public void setApproveAmount(String approveAmount) {
        this.approveAmount = approveAmount == null ? null : approveAmount.trim();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public String getProdType() {
        return prodType;
    }

    public void setProdType(String prodType) {
        this.prodType = prodType == null ? null : prodType.trim();
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score == null ? null : score.trim();
    }

    public String getCallTimes() {
        return callTimes;
    }

    public void setCallTimes(String callTimes) {
        this.callTimes = callTimes == null ? null : callTimes.trim();
    }

    public String getCallAccessScore() {
        return callAccessScore;
    }

    public void setCallAccessScore(String callAccessScore) {
        this.callAccessScore = callAccessScore == null ? null : callAccessScore.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade == null ? null : grade.trim();
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount == null ? null : totalAmount.trim();
    }

    public String getSurplusAmount() {
        return surplusAmount;
    }

    public void setSurplusAmount(String surplusAmount) {
        this.surplusAmount = surplusAmount == null ? null : surplusAmount.trim();
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid == null ? null : pid.trim();
    }

    public String getPchannel() {
        return pchannel;
    }

    public void setPchannel(String pchannel) {
        this.pchannel = pchannel == null ? null : pchannel.trim();
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName == null ? null : channelName.trim();
    }

    public String getMarketPurpose() {
        return marketPurpose;
    }

    public void setMarketPurpose(String marketPurpose) {
        this.marketPurpose = marketPurpose == null ? null : marketPurpose.trim();
    }

    public String getRiskControlLabel() {
        return riskControlLabel;
    }

    public void setRiskControlLabel(String riskControlLabel) {
        this.riskControlLabel = riskControlLabel == null ? null : riskControlLabel.trim();
    }

    public String getFirstLoginTimeStr() {
        return firstLoginTimeStr;
    }

    public void setFirstLoginTimeStr(String firstLoginTimeStr) {
        this.firstLoginTimeStr = firstLoginTimeStr == null ? null : firstLoginTimeStr.trim();
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public String getGoalsApp() {
        return goalsApp;
    }

    public void setGoalsApp(String goalsApp) {
        this.goalsApp = goalsApp == null ? null : goalsApp.trim();
    }

    public String getFlowSideName() {
        return flowSideName;
    }

    public void setFlowSideName(String flowSideName) {
        this.flowSideName = flowSideName == null ? null : flowSideName.trim();
    }

    public String getFlowSidePath() {
        return flowSidePath;
    }

    public void setFlowSidePath(String flowSidePath) {
        this.flowSidePath = flowSidePath == null ? null : flowSidePath.trim();
    }

    public String getCusTag() {
        return cusTag;
    }

    public void setCusTag(String cusTag) {
        this.cusTag = cusTag == null ? null : cusTag.trim();
    }

    public String getAbgroupPushOffsetStr() {
        return abgroupPushOffsetStr;
    }

    public void setAbgroupPushOffsetStr(String abgroupPushOffsetStr) {
        this.abgroupPushOffsetStr = abgroupPushOffsetStr == null ? null : abgroupPushOffsetStr.trim();
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1 == null ? null : extra1.trim();
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2 == null ? null : extra2.trim();
    }

    public String getExtra3() {
        return extra3;
    }

    public void setExtra3(String extra3) {
        this.extra3 = extra3 == null ? null : extra3.trim();
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1 == null ? null : reserveField1.trim();
    }

    public String getCreditTimeStr() {
        return creditTimeStr;
    }

    public void setCreditTimeStr(String creditTimeStr) {
        this.creditTimeStr = creditTimeStr == null ? null : creditTimeStr.trim();
    }

    public String getCreditChannel() {
        return creditChannel;
    }

    public void setCreditChannel(String creditChannel) {
        this.creditChannel = creditChannel == null ? null : creditChannel.trim();
    }

    public String getAmountStatus() {
        return amountStatus;
    }

    public void setAmountStatus(String amountStatus) {
        this.amountStatus = amountStatus == null ? null : amountStatus.trim();
    }

    public String getConnectTimes() {
        return connectTimes;
    }

    public void setConnectTimes(String connectTimes) {
        this.connectTimes = connectTimes == null ? null : connectTimes.trim();
    }

    public String getZyApplyFlag() {
        return zyApplyFlag;
    }

    public void setZyApplyFlag(String zyApplyFlag) {
        this.zyApplyFlag = zyApplyFlag == null ? null : zyApplyFlag.trim();
    }

    public String getZyApplySuccessFlag() {
        return zyApplySuccessFlag;
    }

    public void setZyApplySuccessFlag(String zyApplySuccessFlag) {
        this.zyApplySuccessFlag = zyApplySuccessFlag == null ? null : zyApplySuccessFlag.trim();
    }

    public String getZyAmountStatus() {
        return zyAmountStatus;
    }

    public void setZyAmountStatus(String zyAmountStatus) {
        this.zyAmountStatus = zyAmountStatus == null ? null : zyAmountStatus.trim();
    }

    public String getZyTotalUsableAmount() {
        return zyTotalUsableAmount;
    }

    public void setZyTotalUsableAmount(String zyTotalUsableAmount) {
        this.zyTotalUsableAmount = zyTotalUsableAmount == null ? null : zyTotalUsableAmount.trim();
    }

    public String getIsIdnumber() {
        return isIdnumber;
    }

    public void setIsIdnumber(String isIdnumber) {
        this.isIdnumber = isIdnumber == null ? null : isIdnumber.trim();
    }

    public String getIsTaobao() {
        return isTaobao;
    }

    public void setIsTaobao(String isTaobao) {
        this.isTaobao = isTaobao == null ? null : isTaobao.trim();
    }

    public String getIsNuclearapproval() {
        return isNuclearapproval;
    }

    public void setIsNuclearapproval(String isNuclearapproval) {
        this.isNuclearapproval = isNuclearapproval == null ? null : isNuclearapproval.trim();
    }

    public String getCallaccessscore() {
        return callaccessscore;
    }

    public void setCallaccessscore(String callaccessscore) {
        this.callaccessscore = callaccessscore == null ? null : callaccessscore.trim();
    }

    public String getMarketingScore() {
        return marketingScore;
    }

    public void setMarketingScore(String marketingScore) {
        this.marketingScore = marketingScore == null ? null : marketingScore.trim();
    }

    public String getNoWithdrawOrders() {
        return noWithdrawOrders;
    }

    public void setNoWithdrawOrders(String noWithdrawOrders) {
        this.noWithdrawOrders = noWithdrawOrders == null ? null : noWithdrawOrders.trim();
    }

    public String getPlanData() {
        return planData;
    }

    public void setPlanData(String planData) {
        this.planData = planData == null ? null : planData.trim();
    }

    public String getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(String priorityScore) {
        this.priorityScore = priorityScore == null ? null : priorityScore.trim();
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType == null ? null : callType.trim();
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
}