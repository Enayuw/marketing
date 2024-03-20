package com.br.marketing.entity;

import java.util.Date;

public class XieChengCollidingDataRobArchive {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 规则包记录id
     */
    private Long packageId;

    /**
     * 数据来源类型：T -周期，F-代表非周期
     */
    private String dataSourceType;

    /**
     * 手机号
     */
    private String cellSha256CodeList;

    /**
     * 数据释放时间，下次撞库时间
     */
    private Date releaseTime;

    /**
     * 最近一次撞库时间
     */
    private Date pushTime;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 扩展字段
     */
    private String extend;

    public XieChengCollidingDataRobArchive(){}
    public XieChengCollidingDataRobArchive(XieChengCollidingDataRob xieChengCollidingDataRob){
        this.packageId = xieChengCollidingDataRob.getPackageId();
        this.dataSourceType = xieChengCollidingDataRob.getDataSourceType();
        this.cellSha256CodeList = xieChengCollidingDataRob.getCellSha256CodeList();
        this.releaseTime = xieChengCollidingDataRob.getReleaseTime();
        this.pushTime = xieChengCollidingDataRob.getPushTime();
        this.isDelete = xieChengCollidingDataRob.getIsDelete();
        this.createTime = xieChengCollidingDataRob.getCreateTime();
        this.updateTime = xieChengCollidingDataRob.getUpdateTime();
        this.retryCount = xieChengCollidingDataRob.getRetryCount();
        this.extend = xieChengCollidingDataRob.getExtend();
    }


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

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType == null ? null : dataSourceType.trim();
    }

    public String getCellSha256CodeList() {
        return cellSha256CodeList;
    }

    public void setCellSha256CodeList(String cellSha256CodeList) {
        this.cellSha256CodeList = cellSha256CodeList == null ? null : cellSha256CodeList.trim();
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}