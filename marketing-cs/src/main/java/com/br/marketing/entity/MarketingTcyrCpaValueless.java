package com.br.marketing.entity;

import java.util.Date;

public class MarketingTcyrCpaValueless {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 同一批次下具体单个文件的id
     */
    private Long syncFileId;

    /**
     * 失败类型 1-黑名单；2-已被锁定; 4-无此用户
     */
    private Integer failMsg;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     *  0:异常数据 1:正常数据
     */
    private Integer status;

    /**
     * 删除状态 1-可用 9-删除
     */
    private Integer isDel;

    /**
     * 扩展字段
     */
    private String extend;

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

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public Long getSyncFileId() {
        return syncFileId;
    }

    public void setSyncFileId(Long syncFileId) {
        this.syncFileId = syncFileId;
    }

    public Integer getFailMsg() {
        return failMsg;
    }

    public void setFailMsg(Integer failMsg) {
        this.failMsg = failMsg;
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

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}