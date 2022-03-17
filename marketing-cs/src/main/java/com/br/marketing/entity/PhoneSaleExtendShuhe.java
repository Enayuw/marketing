package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * b_phone_sale_extend_shuhe
 * @author 
 */
public class PhoneSaleExtendShuhe implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 电销表记录id
     */
    private Long pId;

    /**
     * 电销表批次id
     */
    private Long localId;

    /**
     * 案件编号
     */
    private String custNum;

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

    private static final long serialVersionUID = 1L;

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
        this.apiCode = apiCode;
    }

    public Long getpId() {
        return pId;
    }

    public void setpId(Long pId) {
        this.pId = pId;
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public String getCustNum() {
        return custNum;
    }

    public void setCustNum(String custNum) {
        this.custNum = custNum;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
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
}