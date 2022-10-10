package com.br.marketing.entity;

import java.util.Date;

public class MarketingSyncUser {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 客户批次号
     */
    private String cusBatch;

    /**
     * 请求批次
     */
    private String requestBatch;

    /**
     * 用户唯一编号
     */
    private String custNum;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 姓名
     */
    private String name;

    /**
     * 电话
     */
    private String cell;

    /**
     * 场景
     */
    private String groupType;

    /**
     * 新场景-替代group_type
     */
    private String userType;

    /**
     * 日期
     */
    private String registerDate;

    /**
     * 预留字段1
     */
    private String reserveField1;

    /**
     * 预留字段2
     */
    private String reserveField2;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 执行日期
     */
    private String appletDate;

    /**
     * 预留剔除状态字段 1：正常，2：剔除
     */
    private Integer status;

    /**
     * 类型 MD5、Sha256
     */
    private String failType;

    /**
     * 用户上传时间
     */
    private Date appletTime;

    /**
     * 是否导入任务数据 1-未导入;2-导入
     */
    private Integer isTask;

    /**
     * 导入任务用户表时间
     */
    private Date taskTime;

    /**
     * 是否重复 1-未去重; 2-不重复;3-重复;
     */
    private Integer isRepeat;

    public MarketingSyncUser(Long id, String apiCode, String cusBatch, String requestBatch, String custNum, String idCard, String name, String cell, String groupType, String userType, String registerDate, String reserveField1, String reserveField2, Date createTime, Date updateTime, String appletDate, Integer status, String failType, Date appletTime, Integer isTask, Date taskTime, Integer isRepeat) {
        this.id = id;
        this.apiCode = apiCode;
        this.cusBatch = cusBatch;
        this.requestBatch = requestBatch;
        this.custNum = custNum;
        this.idCard = idCard;
        this.name = name;
        this.cell = cell;
        this.groupType = groupType;
        this.userType = userType;
        this.registerDate = registerDate;
        this.reserveField1 = reserveField1;
        this.reserveField2 = reserveField2;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.appletDate = appletDate;
        this.status = status;
        this.failType = failType;
        this.appletTime = appletTime;
        this.isTask = isTask;
        this.taskTime = taskTime;
        this.isRepeat = isRepeat;
    }

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

    public String getCusBatch() {
        return cusBatch;
    }

    public void setCusBatch(String cusBatch) {
        this.cusBatch = cusBatch == null ? null : cusBatch.trim();
    }

    public String getRequestBatch() {
        return requestBatch;
    }

    public void setRequestBatch(String requestBatch) {
        this.requestBatch = requestBatch == null ? null : requestBatch.trim();
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum == null ? null : custNum.trim();
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard == null ? null : idCard.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType == null ? null : groupType.trim();
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate == null ? null : registerDate.trim();
    }

    public String getReserveField1() {
        return reserveField1;
    }

    public void setReserveField1(String reserveField1) {
        this.reserveField1 = reserveField1 == null ? null : reserveField1.trim();
    }

    public String getReserveField2() {
        return reserveField2;
    }

    public void setReserveField2(String reserveField2) {
        this.reserveField2 = reserveField2 == null ? null : reserveField2.trim();
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

    public String getAppletDate() {
        return appletDate;
    }

    public void setAppletDate(String appletDate) {
        this.appletDate = appletDate == null ? null : appletDate.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFailType() {
        return failType;
    }

    public void setFailType(String failType) {
        this.failType = failType == null ? null : failType.trim();
    }

    public Date getAppletTime() {
        return appletTime;
    }

    public void setAppletTime(Date appletTime) {
        this.appletTime = appletTime;
    }

    public Integer getIsTask() {
        return isTask;
    }

    public void setIsTask(Integer isTask) {
        this.isTask = isTask;
    }

    public Date getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(Date taskTime) {
        this.taskTime = taskTime;
    }

    public Integer getIsRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(Integer isRepeat) {
        this.isRepeat = isRepeat;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public MarketingSyncUser() {
    }

    public MarketingSyncUser(String cell) {
        this.cell = cell;
    }
}