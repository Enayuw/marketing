package com.br.marketing.entity;

import java.util.Date;

public class CallRecord {
    /**
     * 
     */
    private Long id;

    /**
     * 公司ID
     */
    private Integer cid;

    /**
     * 
     */
    private String apiCode;

    /**
     * 回调参数类型(1:拨打结果 2:短信发送结果)
     */
    private Integer callBackType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务编号
     */
    private Integer taskId;

    /**
     * 案件编号
     */
    private String caseNum;

    /**
     * 案件状态(0-导入中;1-等待外呼/导入成功;2-导入失败;3-上传失败;4-正在外呼;5-已完成;6-黑名单;7-案件受限;8-暂停;9-取消;10-中原银行-账户余额已足;11-自有tts准备中;12-外呼失败;13-接通限制;14-敏感;15-已转化;16-已失效;)
     */
    private Integer caseStatus;

    /**
     * 案件拨打次数
     */
    private Integer dialCount;

    /**
     * 通话记录编号
     */
    private String sessionId;

    /**
     * 开始外呼时间
     */
    private Date callStartTime;

    /**
     * 外呼接通时间
     */
    private Date callConnectTime;

    /**
     * 外呼结束时间
     */
    private Date callEndTime;

    /**
     * 对话轮次
     */
    private Integer dialogTurn;

    /**
     * 通话状态(1-已接听;2-空号;3-关机;4-停机;5-无人接听;6-无法接通;7-通话中;8-呼叫失败;9-来电提醒;10-用户挂断;11-号码有误/不存在;12-黑名单;13-呼叫限制;14-无需拨打;15-接通限制;16-敏感;17-已转化;18-已失效;)
     */
    private Integer callStatus;

    /**
     * 是否接通(0-否;1-是)
     */
    private Integer isConnect;

    /**
     * 交互文本
     */
    private String callDialog;

    /**
     * 用户信息
     */
    private String userProperties;

    /**
     * 第n次拨打
     */
    private Integer dialRounds;

    /**
     * 录音地址
     */
    private String recordingPath;

    /**
     * 意向等级 A级(有明确意向）;B级(可能有意向);C级(明确拒绝);D级(用户忙);E级(拨打失败);F级(无效客户)
     */
    private String intentionGrade;

    /**
     * 标签列表
     */
    private String tagList;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 线路名称
     */
    private String lineName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Integer getCallBackType() {
        return callBackType;
    }

    public void setCallBackType(Integer callBackType) {
        this.callBackType = callBackType;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.trim();
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getCaseNum() {
        return caseNum;
    }

    public void setCaseNum(String caseNum) {
        this.caseNum = caseNum == null ? null : caseNum.trim();
    }

    public Integer getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(Integer caseStatus) {
        this.caseStatus = caseStatus;
    }

    public Integer getDialCount() {
        return dialCount;
    }

    public void setDialCount(Integer dialCount) {
        this.dialCount = dialCount;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId == null ? null : sessionId.trim();
    }

    public Date getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(Date callStartTime) {
        this.callStartTime = callStartTime;
    }

    public Date getCallConnectTime() {
        return callConnectTime;
    }

    public void setCallConnectTime(Date callConnectTime) {
        this.callConnectTime = callConnectTime;
    }

    public Date getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(Date callEndTime) {
        this.callEndTime = callEndTime;
    }

    public Integer getDialogTurn() {
        return dialogTurn;
    }

    public void setDialogTurn(Integer dialogTurn) {
        this.dialogTurn = dialogTurn;
    }

    public Integer getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(Integer callStatus) {
        this.callStatus = callStatus;
    }

    public Integer getIsConnect() {
        return isConnect;
    }

    public void setIsConnect(Integer isConnect) {
        this.isConnect = isConnect;
    }

    public String getCallDialog() {
        return callDialog;
    }

    public void setCallDialog(String callDialog) {
        this.callDialog = callDialog == null ? null : callDialog.trim();
    }

    public String getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(String userProperties) {
        this.userProperties = userProperties == null ? null : userProperties.trim();
    }

    public Integer getDialRounds() {
        return dialRounds;
    }

    public void setDialRounds(Integer dialRounds) {
        this.dialRounds = dialRounds;
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

    public String getTagList() {
        return tagList;
    }

    public void setTagList(String tagList) {
        this.tagList = tagList == null ? null : tagList.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName == null ? null : lineName.trim();
    }
}