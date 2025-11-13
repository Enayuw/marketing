package com.br.marketing.entity;

import java.util.Date;

public class MarketingIndustryTemplate {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

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
     * 系统类型 0-营销中台 1-外呼系统
     */
    private Integer systemType;

    /**
     * 数据类型：0上传，1转化
     */
    private Integer dataType;

    /**
     * 接收类型：0:通用,1:定制,2:FTP
     */
    private Integer acceptType;

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

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName == null ? null : templateName.trim();
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

    public Integer getSystemType() {
        return systemType;
    }

    public void setSystemType(Integer systemType) {
        this.systemType = systemType;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getAcceptType() {
        return acceptType;
    }

    public void setAcceptType(Integer acceptType) {
        this.acceptType = acceptType;
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