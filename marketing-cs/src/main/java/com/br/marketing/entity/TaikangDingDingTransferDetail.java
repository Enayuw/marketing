package com.br.marketing.entity;

import java.util.Date;

public class TaikangDingDingTransferDetail {
    /**
     * 
     */
    private Long id;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 外呼时间
     */
    private String browseDate;

    /**
     * 姓名
     */
    private String applicationName;

    /**
     * return_result1
     */
    private String returnResult1;

    /**
     * extend
     */
    private String extend;

    /**
     * 推送状态0-未推送  1-推送中 2-推送成功 3-推送失败
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

    /**
     * 数据状态 0-异常数据 1-正常数据
     */
    private Integer status;

    /**
     * 异常数据原因 1-手机号cell非MD5 2-拨打时间为空
     */
    private Integer errorMsg;

    /**
     * 泰康api_code
     */
    private String apiCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getBrowseDate() {
        return browseDate;
    }

    public void setBrowseDate(String browseDate) {
        this.browseDate = browseDate == null ? null : browseDate.trim();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName == null ? null : applicationName.trim();
    }

    public String getReturnResult1() {
        return returnResult1;
    }

    public void setReturnResult1(String returnResult1) {
        this.returnResult1 = returnResult1 == null ? null : returnResult1.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(Integer errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }
}