package com.br.marketing.entity;

import java.util.Date;

public class CallRecording {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 合作客户ID
     */
    private String cid;

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
    private String custNum;

    /**
     * 拨打明细详情
     */
    private Object detail;

    /**
     * 通话记录编号
     */
    private String sessionId;

    /**
     * 开始外呼时间(时间戳)
     */
    private Long callStartTime;

    /**
     * 外呼接通时间(时间戳)
     */
    private Long callConnectTime;

    /**
     * 外呼结束时间(时间戳)
     */
    private Long callEndTime;

    /**
     * 对话轮次
     */
    private Integer dialogTurn;

    /**
     * 通话状态(1:已接听;2:空号;3:关机;4:停机;5:无人接听;6:无法接通;7:通话中;8:呼叫失败;9:来电提醒;10:用户挂断;11:号码有误/不存在;12:黑名单;13:呼叫限制;15:接通限制;16:敏感;17:已转化;18:已失效)
     */
    private Integer callStatus;

    /**
     * 是否接通(0:否;1:是)
     */
    private Integer isConnect;

    /**
     * 交互文本
     */
    private String callDialog;

    /**
     * 录音地址
     */
    private String recordingPath;

    /**
     * 意向等级(A,B,C,D,E,F)
     */
    private String intentionGrade;

    /**
     * 标签列表
     */
    private String tagList;

    /**
     * 预留字段1
     */
    private String reserveField1;

    /**
     * 版本号
     */
    private String version;

    /**
     * 状态 0:待推送1:推送中2:推送成功3:推送失败
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 接收日期
     */
    private String receiveDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
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

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum == null ? null : custNum.trim();
    }

    public Object getDetail() {
        return detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId == null ? null : sessionId.trim();
    }

    public Long getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(Long callStartTime) {
        this.callStartTime = callStartTime;
    }

    public Long getCallConnectTime() {
        return callConnectTime;
    }

    public void setCallConnectTime(Long callConnectTime) {
        this.callConnectTime = callConnectTime;
    }

    public Long getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(Long callEndTime) {
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

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1 == null ? null : reserveField1.trim();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate == null ? null : receiveDate.trim();
    }
}