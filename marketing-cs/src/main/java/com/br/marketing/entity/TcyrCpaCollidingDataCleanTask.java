package com.br.marketing.entity;

import java.util.Date;

public class TcyrCpaCollidingDataCleanTask {
    /**
     * 
     */
    private Long id;

    /**
     * 清洗数据包id集合
     */
    private String cleanPackageIds;

    /**
     * 删除数据包id集合
     */
    private String deletePackageIds;

    /**
     * 清洗状态 0-待清洗；1-清洗中；2-清洗完成；3-清洗失败
     */
    private Integer cleanStatus;

    /**
     * 删除状态 1-可用 9-删除
     */
    private Integer isDel;

    /**
     * 执行信息
     */
    private String executeInfo;

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

    public String getCleanPackageIds() {
        return cleanPackageIds;
    }

    public void setCleanPackageIds(String cleanPackageIds) {
        this.cleanPackageIds = cleanPackageIds == null ? null : cleanPackageIds.trim();
    }

    public String getDeletePackageIds() {
        return deletePackageIds;
    }

    public void setDeletePackageIds(String deletePackageIds) {
        this.deletePackageIds = deletePackageIds == null ? null : deletePackageIds.trim();
    }

    public Integer getCleanStatus() {
        return cleanStatus;
    }

    public void setCleanStatus(Integer cleanStatus) {
        this.cleanStatus = cleanStatus;
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public String getExecuteInfo() {
        return executeInfo;
    }

    public void setExecuteInfo(String executeInfo) {
        this.executeInfo = executeInfo == null ? null : executeInfo.trim();
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