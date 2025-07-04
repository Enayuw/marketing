package com.br.marketing.entity;

import java.math.BigDecimal;
import java.util.Date;

public class MarketingSmsAccountLog {
    /**
     * 
     */
    private Long id;

    /**
     * 汇总记录id
     */
    private Long recordId;

    /**
     * 供应商名称
     */
    private String vendorName;

    /**
     * 渠道名称集合
     */
    private String channelsName;

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
     * 操作人
     */
    private String createdBy;

    /**
     * 业务删除 1-新增 2-变更 3-删除
     */
    private Integer opeType;

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

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName == null ? null : vendorName.trim();
    }

    public String getChannelsName() {
        return channelsName;
    }

    public void setChannelsName(String channelsName) {
        this.channelsName = channelsName == null ? null : channelsName.trim();
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public Integer getOpeType() {
        return opeType;
    }

    public void setOpeType(Integer opeType) {
        this.opeType = opeType;
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