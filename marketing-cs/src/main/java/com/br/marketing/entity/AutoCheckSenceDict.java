package com.br.marketing.entity;

import java.util.Date;

public class AutoCheckSenceDict {
    /**
     * 
     */
    private Long id;

    /**
     * 场景编码
     */
    private String senceCode;

    /**
     * 场景名称
     */
    private String senceName;

    /**
     * 0-未删除 1-删除
     */
    private Byte isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenceCode() {
        return senceCode;
    }

    public void setSenceCode(String senceCode) {
        this.senceCode = senceCode == null ? null : senceCode.trim();
    }

    public String getSenceName() {
        return senceName;
    }

    public void setSenceName(String senceName) {
        this.senceName = senceName == null ? null : senceName.trim();
    }

    public Byte getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Byte isDeleted) {
        this.isDeleted = isDeleted;
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