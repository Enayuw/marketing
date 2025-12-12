package com.br.marketing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class SmsAccountDetailNormal {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 供应商组合id
     */
    private Long vendorId;

    /**
     * 线路id
     */
    private Long channelId;

    /**
     * 短信单价，元/条
     */
    private BigDecimal price;

    /**
     * 生效开始日期
     */
    private Date effectStartDate;

    /**
     * 生效结束日期
     */
    private Date effectEndDate;

    /**
     * 禁用标志 0-禁用 1-启用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getEffectStartDate() {
        return effectStartDate;
    }

    public void setEffectStartDate(Date effectStartDate) {
        this.effectStartDate = effectStartDate;
    }

    public Date getEffectEndDate() {
        return effectEndDate;
    }

    public void setEffectEndDate(Date effectEndDate) {
        this.effectEndDate = effectEndDate;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
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

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}