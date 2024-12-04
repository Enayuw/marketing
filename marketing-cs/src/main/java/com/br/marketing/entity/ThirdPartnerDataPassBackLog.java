package com.br.marketing.entity;

import java.util.Date;

public class ThirdPartnerDataPassBackLog {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 上传明细id
     */
    private Long syncUserId;

    /**
     * 原始客户编号
     */
    private String orgApiCode;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 手机号log加密
     */
    private String cell;

    /**
     * 推送任务id
     */
    private Long taskId;

    /**
     * 状态 0-推送中、1-推送成功、3-推送失败
     */
    private Integer status;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDeleted;

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

    public Long getSyncUserId() {
        return syncUserId;
    }

    public void setSyncUserId(Long syncUserId) {
        this.syncUserId = syncUserId;
    }

    public String getOrgApiCode() {
        return orgApiCode;
    }

    public void setOrgApiCode(String orgApiCode) {
        this.orgApiCode = orgApiCode == null ? null : orgApiCode.trim();
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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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