package com.br.marketing.entity;

import java.util.Date;

public class MarketingSceneVariable {
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
     * 版本号
     */
    private String version;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 场景代码
     */
    private String sceneCode;

    /**
     * 数据标识
     */
    private String taskUid;

    /**
     * 数据列表（JSON格式）
     */
    private String variableList;

    /**
     * 执行状态：0待执行、1执行中、2执行完成
     */
    private Integer executeStatus;

    /**
     * 执行结果
     */
    private String executeResult;

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

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode == null ? null : sceneCode.trim();
    }

    public String getTaskUid() {
        return taskUid;
    }

    public void setTaskUid(String taskUid) {
        this.taskUid = taskUid == null ? null : taskUid.trim();
    }

    public String getVariableList() {
        return variableList;
    }

    public void setVariableList(String variableList) {
        this.variableList = variableList == null ? null : variableList.trim();
    }

    public Integer getExecuteStatus() {
        return executeStatus;
    }

    public void setExecuteStatus(Integer executeStatus) {
        this.executeStatus = executeStatus;
    }

    public String getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(String executeResult) {
        this.executeResult = executeResult == null ? null : executeResult.trim();
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