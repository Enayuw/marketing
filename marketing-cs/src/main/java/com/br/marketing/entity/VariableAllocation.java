package com.br.marketing.entity;

import java.util.Date;

public class VariableAllocation {
    /**
     *
     */
    private Integer id;

    /**
     *
     */
    private String apiCode;

    /**
     *
     */
    private String allocationType;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     *
     */
    private String allocationValue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(String allocationType) {
        this.allocationType = allocationType == null ? null : allocationType.trim();
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

    public String getAllocationValue() {
        return allocationValue;
    }

    public void setAllocationValue(String allocationValue) {
        this.allocationValue = allocationValue == null ? null : allocationValue.trim();
    }
}