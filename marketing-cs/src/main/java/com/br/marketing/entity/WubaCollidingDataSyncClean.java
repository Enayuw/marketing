package com.br.marketing.entity;

import java.util.Date;

public class WubaCollidingDataSyncClean {
    /**
     * 
     */
    private Long id;

    /**
     * md5手机号
     */
    private String cell;

    /**
     * 清洗状态 清洗状态 0-待清洗 1-清洗中，2-已完成
     */
    private Integer cleanStatus;

    /**
     *  扩展字段
     */
    private String extend;

    /**
     *  状态 0-正常 1-删除
     */
    private Integer isDelete;

    /**
     *  创建时间
     */
    private Date createTime;

    /**
     *  修改时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public Integer getCleanStatus() {
        return cleanStatus;
    }

    public void setCleanStatus(Integer cleanStatus) {
        this.cleanStatus = cleanStatus;
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