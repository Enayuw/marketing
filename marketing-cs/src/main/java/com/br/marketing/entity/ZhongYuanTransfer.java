package com.br.marketing.entity;

import java.util.Date;

public class ZhongYuanTransfer {
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
     * 批次编号
     */
    private String batchUid;

    /**
     * 案件编号列表
     */
    private String taskuidList;

    /**
     * 操作
     */
    private String operation;

    /**
     * 清洗状态 0-待清洗 1-清洗中 2-清洗完成
     */
    private Integer cleanStatus;

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

    public String getBatchUid() {
        return batchUid;
    }

    public void setBatchUid(String batchUid) {
        this.batchUid = batchUid == null ? null : batchUid.trim();
    }

    public String getTaskuidList() {
        return taskuidList;
    }

    public void setTaskuidList(String taskuidList) {
        this.taskuidList = taskuidList == null ? null : taskuidList.trim();
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation == null ? null : operation.trim();
    }

    public Integer getCleanStatus() {
        return cleanStatus;
    }

    public void setCleanStatus(Integer cleanStatus) {
        this.cleanStatus = cleanStatus;
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