package com.br.marketing.entity;

import java.util.Date;

public class XieChengCollidingDataHitRequestNoMapping {
    /**
     * 
     */
    private Long id;

    /**
     * 撞库日志表id
     */
    private Long logId;

    /**
     * 撞库请求流水号
     */
    private String hitRequestNo;

    /**
     * sha256手机号
     */
    private String cellSha256CodeList;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 撞库日期
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

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getHitRequestNo() {
        return hitRequestNo;
    }

    public void setHitRequestNo(String hitRequestNo) {
        this.hitRequestNo = hitRequestNo == null ? null : hitRequestNo.trim();
    }

    public String getCellSha256CodeList() {
        return cellSha256CodeList;
    }

    public void setCellSha256CodeList(String cellSha256CodeList) {
        this.cellSha256CodeList = cellSha256CodeList == null ? null : cellSha256CodeList.trim();
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