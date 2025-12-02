package com.br.marketing.entity;

import java.util.Date;

public class LineSupplierInfoNormal {
    /**
     * 
     */
    private Long id;

    /**
     * 线路商
     */
    private String lineSupplier;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 状态 0-没修改 1-已修改 2-已删除
     */
    private Integer opeStatus;

    /**
     * 修改时间
     */
    private Date opeTime;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLineSupplier() {
        return lineSupplier;
    }

    public void setLineSupplier(String lineSupplier) {
        this.lineSupplier = lineSupplier == null ? null : lineSupplier.trim();
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

    public Integer getOpeStatus() {
        return opeStatus;
    }

    public void setOpeStatus(Integer opeStatus) {
        this.opeStatus = opeStatus;
    }

    public Date getOpeTime() {
        return opeTime;
    }

    public void setOpeTime(Date opeTime) {
        this.opeTime = opeTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}