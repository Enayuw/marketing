package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class XieChengCollidingDataLoopCycleArchive {
    /**
     * 主键
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

    public XieChengCollidingDataLoopCycleArchive(){}

    public XieChengCollidingDataLoopCycleArchive(XieChengCollidingDataLoopCycle xieChengCollidingDataLoopCycle){
        this.packageId = xieChengCollidingDataLoopCycle.getPackageId();
        this.dataSourceType = xieChengCollidingDataLoopCycle.getDataSourceType();
        this.cellSha256CodeList = xieChengCollidingDataLoopCycle.getCellSha256CodeList();
        this.releaseTime = xieChengCollidingDataLoopCycle.getReleaseTime();
        this.isDelete = xieChengCollidingDataLoopCycle.getIsDelete();
        this.createTime = xieChengCollidingDataLoopCycle.getCreateTime();
        this.updateTime = xieChengCollidingDataLoopCycle.getUpdateTime();
        this.retryCount = xieChengCollidingDataLoopCycle.getRetryCount();
        this.extend = xieChengCollidingDataLoopCycle.getExtend();
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