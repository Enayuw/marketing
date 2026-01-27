package com.br.marketing.entity;

import java.util.Date;

public class DiDiDataLoopCycle {
    /**
     * 主键id
     */
    private Long id;

    /**
     *
     */
    private String apiCode;

    /**
     * 锁定类型，1-我司，2-频控限制
     */
    private Integer lockType;

    /**
     * 数据来源，T-周期，F-非周期
     */
    private String sourceType;

    /**
     * 实际撞库时间
     */
    private Date pushTime;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 撞库时间
     */
    private Date collidingTime;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-正常，1-非正常
     */
    private Integer isDelete;

    /**
     * 创建日期
     */
    private Integer createDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 数据包id
     */
    private String packageId;

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

    public Integer getLockType() {
        return lockType;
    }

    public void setLockType(Integer lockType) {
        this.lockType = lockType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType == null ? null : sourceType.trim();
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public Date getCollidingTime() {
        return collidingTime;
    }

    public void setCollidingTime(Date collidingTime) {
        this.collidingTime = collidingTime;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Integer createDate) {
        this.createDate = createDate;
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

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId == null ? null : packageId.trim();
    }
}