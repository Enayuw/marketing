package com.br.marketing.entity;

import java.util.Date;

public class MarketingIndustryTemplate {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 一级部门
     */
    private String firstDepartment;

    /**
     * 二级部门
     */
    private String secondDepartment;

    /**
     * 三级部门
     */
    private String apiType;

    /**
     * 接口id
     */
    private Long interfaceTemplateId;

    /**
     * 删除标志；1-正常；9-删除
     */
    private Integer isDel;

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

    public String getFirstDepartment() {
        return firstDepartment;
    }

    public void setFirstDepartment(String firstDepartment) {
        this.firstDepartment = firstDepartment == null ? null : firstDepartment.trim();
    }

    public String getSecondDepartment() {
        return secondDepartment;
    }

    public void setSecondDepartment(String secondDepartment) {
        this.secondDepartment = secondDepartment == null ? null : secondDepartment.trim();
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType == null ? null : apiType.trim();
    }

    public Long getInterfaceTemplateId() {
        return interfaceTemplateId;
    }

    public void setInterfaceTemplateId(Long interfaceTemplateId) {
        this.interfaceTemplateId = interfaceTemplateId;
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
}