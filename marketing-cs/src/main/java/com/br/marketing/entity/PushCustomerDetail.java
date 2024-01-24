package com.br.marketing.entity;

import java.util.Date;

public class PushCustomerDetail {
    /**
     * 
     */
    private Long id;

    /**
     * 跑分结果唯一id
     */
    private String scoreId;

    /**
     * 跑分记录id
     */
    private Long fileId;

    /**
     * apicode
     */
    private String apiCode;

    /**
     * 案件号
     */
    private String custNum;

    /**
     * 场景
     */
    private String userType;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 数据批次号
     */
    private String taskId;

    /**
     * 请求批次号
     */
    private String requestId;

    /**
     * 推送的扩展信息
     */
    private String pushJson;

    /**
     * 分数顺序字段1
     */
    private String scoreSort1;

    /**
     * 分数顺序字段2
     */
    private String scoreSort2;

    /**
     * 分数顺序字段3
     */
    private String scoreSort3;

    /**
     * 分数顺序字段4
     */
    private String scoreSort4;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 1-未推送；2-推送
     */
    private Integer pushStatus;

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

    public String getScoreId() {
        return scoreId;
    }

    public void setScoreId(String scoreId) {
        this.scoreId = scoreId == null ? null : scoreId.trim();
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getPushJson() {
        return pushJson;
    }

    public void setPushJson(String pushJson) {
        this.pushJson = pushJson == null ? null : pushJson.trim();
    }

    public String getScoreSort1() {
        return scoreSort1;
    }

    public void setScoreSort1(String scoreSort1) {
        this.scoreSort1 = scoreSort1 == null ? null : scoreSort1.trim();
    }

    public String getScoreSort2() {
        return scoreSort2;
    }

    public void setScoreSort2(String scoreSort2) {
        this.scoreSort2 = scoreSort2 == null ? null : scoreSort2.trim();
    }

    public String getScoreSort3() {
        return scoreSort3;
    }

    public void setScoreSort3(String scoreSort3) {
        this.scoreSort3 = scoreSort3 == null ? null : scoreSort3.trim();
    }

    public String getScoreSort4() {
        return scoreSort4;
    }

    public void setScoreSort4(String scoreSort4) {
        this.scoreSort4 = scoreSort4 == null ? null : scoreSort4.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
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