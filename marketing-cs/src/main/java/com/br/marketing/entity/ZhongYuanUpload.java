package com.br.marketing.entity;

import java.util.Date;

public class ZhongYuanUpload {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 全局唯一流水号
     */
    private String flowId;

    /**
     * 系统标识，默认cs
     */
    private String sysId;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 厂商编号
     */
    private String channelNo;

    /**
     * 版本号，默认1.0
     */
    private String version;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 批次名称
     */
    private String batchName;

    /**
     * 批次编号
     */
    private String batchNo;

    /**
     * 场景代码
     */
    private String sceneCode;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 节假日禁止标识
     */
    private String festivalBan;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 上报结束标识
     */
    private String reportEndFlag;

    /**
     * 任务数据列表（JSON格式）
     */
    private String taskdataList;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
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

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId == null ? null : flowId.trim();
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId == null ? null : sysId.trim();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp == null ? null : timestamp.trim();
    }

    public String getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(String channelNo) {
        this.channelNo = channelNo == null ? null : channelNo.trim();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName == null ? null : batchName.trim();
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode == null ? null : sceneCode.trim();
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime == null ? null : startTime.trim();
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime == null ? null : endTime.trim();
    }

    public String getFestivalBan() {
        return festivalBan;
    }

    public void setFestivalBan(String festivalBan) {
        this.festivalBan = festivalBan == null ? null : festivalBan.trim();
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority == null ? null : priority.trim();
    }

    public String getReportEndFlag() {
        return reportEndFlag;
    }

    public void setReportEndFlag(String reportEndFlag) {
        this.reportEndFlag = reportEndFlag == null ? null : reportEndFlag.trim();
    }

    public String getTaskdataList() {
        return taskdataList;
    }

    public void setTaskdataList(String taskdataList) {
        this.taskdataList = taskdataList == null ? null : taskdataList.trim();
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