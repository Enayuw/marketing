package com.br.marketing.entity;

import java.util.Date;

public class MarketingTcyrCpaDataLog {
    /**
     * 
     */
    private Long id;

    /**
     * 数据id
     */
    private Long cpaCollidingDataId;

    /**
     * 数据包id
     */
    private Long packageId;

    /**
     * 数据来源：T -周期，F-代表非周期
     */
    private String dataSourceType;

    /**
     * 用户唯一编号
     */
    private String userKey;

    /**
     * 电话
     */
    private String cell;

    /**
     * 释放时间
     */
    private Date releaseTime;

    /**
     * 接收时间
     */
    private Date receiveTime;

    /**
     * 是否锁定 true：锁定中，false：非锁定中
     */
    private Boolean result;

    /**
     * 原始文本
     */
    private String originText;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 删除状态 1-可用 9-删除
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

    /**
     * mq发送的request_id，幂等判断
     */
    private String requestId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCpaCollidingDataId() {
        return cpaCollidingDataId;
    }

    public void setCpaCollidingDataId(Long cpaCollidingDataId) {
        this.cpaCollidingDataId = cpaCollidingDataId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType == null ? null : dataSourceType.trim();
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey == null ? null : userKey.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getOriginText() {
        return originText;
    }

    public void setOriginText(String originText) {
        this.originText = originText == null ? null : originText.trim();
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }
}