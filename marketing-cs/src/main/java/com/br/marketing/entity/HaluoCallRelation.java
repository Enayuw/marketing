package com.br.marketing.entity;

import java.util.Date;

public class HaluoCallRelation {
    /**
     * 
     */
    private Long id;

    /**
     * 转化id
     */
    private Long transferId;

    /**
     * 电销批次id
     */
    private Long dxId;

    /**
     * 黑名单批次id
     */
    private Long blackId;

    /**
     * 转化表requestid
     */
    private String requestId;

    /**
     * 状态 1-有效；9-失效
     */
    private Integer isDel;

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

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public Long getDxId() {
        return dxId;
    }

    public void setDxId(Long dxId) {
        this.dxId = dxId;
    }

    public Long getBlackId() {
        return blackId;
    }

    public void setBlackId(Long blackId) {
        this.blackId = blackId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
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