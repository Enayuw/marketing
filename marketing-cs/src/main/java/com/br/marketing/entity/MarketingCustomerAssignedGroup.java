package com.br.marketing.entity;

import lombok.Getter;

import java.util.Date;

@Getter
public class MarketingCustomerAssignedGroup {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户ID
     */
    private String cid;

    /**
     * 开发组
     */
    private String assignedGroup;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public void setId(Long id) {
        this.id = id;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public void setAssignedGroup(String assignedGroup) {
        this.assignedGroup = assignedGroup == null ? null : assignedGroup.trim();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}