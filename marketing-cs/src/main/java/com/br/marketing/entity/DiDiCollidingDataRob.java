package com.br.marketing.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class DiDiCollidingDataRob {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 数据id（未落库）
     */
    @Setter
    @Getter
    private Long dataId;

    /**
     * package id
     */
    private Long packageId;

    /**
     * 
     */
    private String apiCode;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 数据来源，T-周期，F-非周期
     */
    private String sourceType;

    /**
     * 撞库时间
     */
    private Date collidingTime;

    /**
     * 实际撞库时间
     */
    private Date pushTime;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType == null ? null : sourceType.trim();
    }

    public Date getCollidingTime() {
        return collidingTime;
    }

    public void setCollidingTime(Date collidingTime) {
        this.collidingTime = collidingTime;
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
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
}