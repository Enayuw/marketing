package com.br.marketing.entity;

import java.util.Date;

public class SoleRuleConfig {
    /**
     * 
     */
    private Long id;

    /**
     * 去重规则名称
     */
    private String soleName;

    /**
     * 去重字段
     */
    private String soleFields;

    /**
     * 开启状态 1-开启；2-禁用；3-开启中
     */
    private Integer status;

    /**
     * 删除标志；1-正常；9-删除；
     */
    private Integer isDel;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 去重时间周期
     */
    private Integer soleCycleTimes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSoleName() {
        return soleName;
    }

    public void setSoleName(String soleName) {
        this.soleName = soleName == null ? null : soleName.trim();
    }

    public String getSoleFields() {
        return soleFields;
    }

    public void setSoleFields(String soleFields) {
        this.soleFields = soleFields == null ? null : soleFields.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Integer getSoleCycleTimes() {
        return soleCycleTimes;
    }

    public void setSoleCycleTimes(Integer soleCycleTimes) {
        this.soleCycleTimes = soleCycleTimes;
    }
}