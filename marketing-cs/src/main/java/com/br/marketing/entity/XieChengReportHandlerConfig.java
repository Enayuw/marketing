package com.br.marketing.entity;

import java.util.Date;

public class XieChengReportHandlerConfig {
    /**
     * 
     */
    private Long id;

    /**
     * 业务形态
     */
    private String bizForm;

    /**
     * handler名称
     */
    private String handlerNames;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBizForm() {
        return bizForm;
    }

    public void setBizForm(String bizForm) {
        this.bizForm = bizForm == null ? null : bizForm.trim();
    }

    public String getHandlerNames() {
        return handlerNames;
    }

    public void setHandlerNames(String handlerNames) {
        this.handlerNames = handlerNames == null ? null : handlerNames.trim();
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