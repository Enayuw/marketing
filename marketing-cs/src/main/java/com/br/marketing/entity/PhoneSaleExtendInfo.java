package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;

public class PhoneSaleExtendInfo implements Serializable {
    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 手机号
     */
    private String cell;

    /**
     * taskId
     */
    private String taskId;

    /**
     * 场景
     */
    private String userType;

    /**
     * 数据上传日期
     */
    private String appletDate;

    /**
     * 数据上传时间
     */
    private String appletTime;

    /**
     * 状态 a,b
     */
    private String status;

    /**
     * 1-未推送；2-推送成功；3-推送失败
     */
    private Integer pStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 客户传输节点
     */
    private String type;

    /**
     * 电销节点
     */
    private String dxType;

    /**
     * 1-实时推送;0-非实时推送
     */
    private String transformType;

    /**
     * 源数据id
     */
    private Long sourceId;

    /**
     * 推送电销时间
     */
    private Date pushDxTime;

    /**
     * 冗余信息
     */
    private String redundancyField;

    /**
     * null或者1——人工批量和人工单条，2——ibu定制人工批量接口
     */
    private Integer interfaceType;

    /**
     * 组号，默认组号为0
     */
    private Integer groupNumber;

    /**
     * 电销业务线使用的场景
     */
    private String dxUserType;

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

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum == null ? null : custNum.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
    }

    public String getAppletTime() {
        return appletTime;
    }

    public void setAppletTime(String appletTime) {
        this.appletTime = appletTime == null ? null : appletTime.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Integer getpStatus() {
        return pStatus;
    }

    public void setpStatus(Integer pStatus) {
        this.pStatus = pStatus;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getDxType() {
        return dxType;
    }

    public void setDxType(String dxType) {
        this.dxType = dxType == null ? null : dxType.trim();
    }

    public String getTransformType() {
        return transformType;
    }

    public void setTransformType(String transformType) {
        this.transformType = transformType == null ? null : transformType.trim();
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Date getPushDxTime() {
        return pushDxTime;
    }

    public void setPushDxTime(Date pushDxTime) {
        this.pushDxTime = pushDxTime;
    }

    public String getRedundancyField() {
        return redundancyField;
    }

    public void setRedundancyField(String redundancyField) {
        this.redundancyField = redundancyField == null ? null : redundancyField.trim();
    }

    public Integer getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(Integer interfaceType) {
        this.interfaceType = interfaceType;
    }

    public Integer getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(Integer groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getDxUserType() {
        return dxUserType;
    }

    public void setDxUserType(String dxUserType) {
        this.dxUserType = dxUserType == null ? null : dxUserType.trim();
    }
}