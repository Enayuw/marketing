package com.br.marketing.entity;

import java.util.Date;

public class ZhonganMarketingBan {
    /**
     * 
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 案件
     */
    private String custNum;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 场景
     */
    private String userType;

    /**
     * taskid
     */
    private String taskId;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 数据上传日期
     */
    private String appletDate;

    /**
     * 撞库日期
     */
    private String zkDate;

    /**
     * 推送客服黑名单状态1-未推送；2-推送
     */
    private Integer pushStatus;

    /**
     * 推送时间
     */
    private Date pushTime;

    /**
     * 上传数据id
     */
    private String initId;

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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
    }

    public String getZkDate() {
        return zkDate;
    }

    public void setZkDate(String zkDate) {
        this.zkDate = zkDate == null ? null : zkDate.trim();
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public String getInitId() {
        return initId;
    }

    public void setInitId(String initId) {
        this.initId = initId == null ? null : initId.trim();
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