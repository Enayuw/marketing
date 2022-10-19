package com.br.marketing.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

public class CustomerCallingDialog {
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
    private Long localId;

    /**
     *
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cid;

    /**
     * 用户类型
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userType;

    /**
     * 用户类型

     */
    private String groupType;

    /**
     * 流水号
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String swiftNumber;

    /**
     * 任务id(上传接口中客户上传的数据)
     */
    private String taskId;

    /**
     * 任务名称
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String taskName;

    /**
     * 任务类型
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer taskType;

    /**
     * 案件编号
     */
    private String caseNum;

    /**
     * 通话编号
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dialogId;

    /**
     * 案件状态见附录
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer caseStatus;

    /**
     * 是否发送短信(0:不发送/1:发送)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isSendMsg;

    /**
     * 批次id，发送时的批次id
     */
    private String requestId;

    /**
     * 是否发送数据到客户端(0:未发送/1: 已发送)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer sendStatus;

    /**
     * 通话状态
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer dropStatus;

    /**
     * 通话时长
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer realCallTime;

    /**
     * 外呼开始时间，首次触达时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String callStartTime;

    /**
     * 外呼接通时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String callConnectTime;

    /**
     * 外呼结束时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String callEndTime;

    /**
     * 第n次拨打
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer dialogRounds;

    /**
     * 通话轮次
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer dialogTurns;

    /**
     * 录音地址
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String recordingPath;

    /**
     * 意向等级(A级【有明确意向】B级【可能有意向】C级【明确拒绝】D级【用户忙】E级【拨打失败】F级【无效客户】)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String intentionGrade;

    /**
     * 通话状态
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer dialogStatus;

    /**
     * 状态 1正常，0删除
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Byte status;

    /**
     * 创建时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date updateTime;

    /**
     * 标签列表
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tagList;

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

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getSwiftNumber() {
        return swiftNumber;
    }

    public void setSwiftNumber(String swiftNumber) {
        this.swiftNumber = swiftNumber == null ? null : swiftNumber.trim();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.trim();
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public String getCaseNum() {
        return caseNum;
    }

    public void setCaseNum(String caseNum) {
        this.caseNum = caseNum == null ? null : caseNum.trim();
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId == null ? null : dialogId.trim();
    }

    public Integer getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(Integer caseStatus) {
        this.caseStatus = caseStatus;
    }

    public Integer getIsSendMsg() {
        return isSendMsg;
    }

    public void setIsSendMsg(Integer isSendMsg) {
        this.isSendMsg = isSendMsg;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public Integer getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(Integer sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Integer getDropStatus() {
        return dropStatus;
    }

    public void setDropStatus(Integer dropStatus) {
        this.dropStatus = dropStatus;
    }

    public Integer getRealCallTime() {
        return realCallTime;
    }

    public void setRealCallTime(Integer realCallTime) {
        this.realCallTime = realCallTime;
    }

    public String getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(String callStartTime) {
        this.callStartTime = callStartTime == null ? null : callStartTime.trim();
    }

    public String getCallConnectTime() {
        return callConnectTime;
    }

    public void setCallConnectTime(String callConnectTime) {
        this.callConnectTime = callConnectTime == null ? null : callConnectTime.trim();
    }

    public String getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(String callEndTime) {
        this.callEndTime = callEndTime == null ? null : callEndTime.trim();
    }

    public Integer getDialogRounds() {
        return dialogRounds;
    }

    public void setDialogRounds(Integer dialogRounds) {
        this.dialogRounds = dialogRounds;
    }

    public Integer getDialogTurns() {
        return dialogTurns;
    }

    public void setDialogTurns(Integer dialogTurns) {
        this.dialogTurns = dialogTurns;
    }

    public String getRecordingPath() {
        return recordingPath;
    }

    public void setRecordingPath(String recordingPath) {
        this.recordingPath = recordingPath == null ? null : recordingPath.trim();
    }

    public String getIntentionGrade() {
        return intentionGrade;
    }

    public void setIntentionGrade(String intentionGrade) {
        this.intentionGrade = intentionGrade == null ? null : intentionGrade.trim();
    }

    public Integer getDialogStatus() {
        return dialogStatus;
    }

    public void setDialogStatus(Integer dialogStatus) {
        this.dialogStatus = dialogStatus;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
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

    public String getTagList() {
        return tagList;
    }

    public void setTagList(String tagList) {
        this.tagList = tagList == null ? null : tagList.trim();
    }
}