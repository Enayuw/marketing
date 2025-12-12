package com.br.marketing.entity;

import java.util.Date;

public class SmsBaseInfoNormal {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 短信线路id
     */
    private Long channelId;

    /**
     * 供应商id
     */
    private Long vendorId;

    /**
     * 短信线路名称
     */
    private String channelName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 状态 0-未修改;1-vendorId改变导致的修改(目前没有这个场景);2-channelName 其它字段导致的修改;3-三方短信侧删除导致的修改
     */
    private Integer opeStatus;

    /**
     * 操作修改时间
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

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName == null ? null : channelName.trim();
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