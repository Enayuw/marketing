package com.br.marketing.entity;

import java.util.Date;

public class XieChengCollidingDataHitRequestNoInit {
    /**
     * 
     */
    private Long id;

    /**
     * sha256手机号
     */
    private String cellSha256CodeList;

    /**
     * 撞库请求流水号
     */
    private String hitRequestNo;

    /**
     * 最近一次撞库时间
     */
    private Date pushTime;

    /**
     * 查询状态 0：未查询、1：已查询、2：查询失败
     */
    private Integer queryStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

    /**
     * 扩展字段
     */
    private String extend;

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

    public String getCellSha256CodeList() {
        return cellSha256CodeList;
    }

    public void setCellSha256CodeList(String cellSha256CodeList) {
        this.cellSha256CodeList = cellSha256CodeList == null ? null : cellSha256CodeList.trim();
    }

    public String getHitRequestNo() {
        return hitRequestNo;
    }

    public void setHitRequestNo(String hitRequestNo) {
        this.hitRequestNo = hitRequestNo == null ? null : hitRequestNo.trim();
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public Integer getQueryStatus() {
        return queryStatus;
    }

    public void setQueryStatus(Integer queryStatus) {
        this.queryStatus = queryStatus;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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