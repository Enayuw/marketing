package com.br.marketing.entity;

import java.util.Date;

public class CustomerSole {
    /**
     *
     */
    private Long id;

    /**
     * 客户id
     */
    private Long customerId;

    /**
     * 去重规则id
     */
    private Long soleId;

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
     * 去重数据范围规则
     */
    private String conditionInfo;

    /**
     * 去重规则使用场景数量
     */
    private Integer userTypeCount;

    /**
     * 是否默认全场景,0:否,仅用配置的场景; 1:是,兼容后续全场景
     */
    private Integer allUserType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getSoleId() {
        return soleId;
    }

    public void setSoleId(Long soleId) {
        this.soleId = soleId;
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

    public String getConditionInfo() {
        return conditionInfo;
    }

    public void setConditionInfo(String conditionInfo) {
        this.conditionInfo = conditionInfo == null ? null : conditionInfo.trim();
    }

    public Integer getUserTypeCount() {
        return userTypeCount;
    }

    public void setUserTypeCount(Integer userTypeCount) {
        this.userTypeCount = userTypeCount;
    }

    public Integer getAllUserType() {
        return allUserType;
    }

    public void setAllUserType(Integer allUserType) {
        this.allUserType = allUserType;
    }
}