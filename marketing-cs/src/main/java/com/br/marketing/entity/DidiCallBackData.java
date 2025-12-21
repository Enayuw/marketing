package com.br.marketing.entity;

import java.util.Date;

public class DidiCallBackData {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 回调参数类型:1-拨打结果,2-短信发送结果
     */
    private Integer callbackType;

    /**
     * 公司ID
     */
    private String cid;

    /**
     * API代码
     */
    private String apiCode;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务编号
     */
    private String taskId;

    /**
     * 案件编号/客户号码
     */
    private String cell;

    /**
     * 案件状态
     */
    private String caseStatus;

    /**
     * 案件拨打次数
     */
    private Integer dialCount;

    /**
     * 拨打明细详情
     */
    private String detail;

    /**
     * 通话记录编号
     */
    private String callRecordId;

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
     * 通话状态
     */
    private String callStatus;

    /**
     * 是否接通:0-否,1-是
     */
    private Integer isConnect;

    /**
     * 短信发送状态:0-否,1-是
     */
    private Integer smsSendStatus;

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
     * 意向等级:A级-有明确意向,B级-可能有意向,C级-明确拒绝,D级-用户忙,E级-拨打失败,F级-无效客户
     */
    private String intentionGrade;

    /**
     * 标签列表
     */
    private String tagList;

    /**
     * 0-待上报，1已上报
     */
    private Integer pushStatus;

    /**
     * 0-正常，1-数据重复
     */
    private Integer status;

    /**
     * 扩展字段，存储其他额外信息
     */
    private String extend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 交互文本
     */
    private String callDialog;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCallbackType() {
        return callbackType;
    }

    public void setCallbackType(Integer callbackType) {
        this.callbackType = callbackType;
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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.trim();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus == null ? null : caseStatus.trim();
    }

    public Integer getDialCount() {
        return dialCount;
    }

    public void setDialCount(Integer dialCount) {
        this.dialCount = dialCount;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

    public String getCallRecordId() {
        return callRecordId;
    }

    public void setCallRecordId(String callRecordId) {
        this.callRecordId = callRecordId == null ? null : callRecordId.trim();
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

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus == null ? null : callStatus.trim();
    }

    public Integer getIsConnect() {
        return isConnect;
    }

    public void setIsConnect(Integer isConnect) {
        this.isConnect = isConnect;
    }

    public Integer getSmsSendStatus() {
        return smsSendStatus;
    }

    public void setSmsSendStatus(Integer smsSendStatus) {
        this.smsSendStatus = smsSendStatus;
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

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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

    public String getCallDialog() {
        return callDialog;
    }

    public void setCallDialog(String callDialog) {
        this.callDialog = callDialog == null ? null : callDialog.trim();
    }
}