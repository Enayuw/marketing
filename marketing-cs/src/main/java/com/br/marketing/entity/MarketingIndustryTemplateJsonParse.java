package com.br.marketing.entity;

import java.util.Date;

public class MarketingIndustryTemplateJsonParse {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 接口id
     */
    private Long interfaceTemplateId;

    /**
     * 接口名称
     */
    private String interfaceTemplateName;

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
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点值
     */
    private String nodeValue;

    /**
     * 父节点完整路径
     */
    private String parentPath;

    /**
     * 节点类型: object, array, primitive
     */
    private String nodeType;

    /**
     * 是否为数组元素
     */
    private Boolean isArrayItem;

    /**
     * 节点层级
     */
    private Integer level;

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

    public Long getInterfaceTemplateId() {
        return interfaceTemplateId;
    }

    public void setInterfaceTemplateId(Long interfaceTemplateId) {
        this.interfaceTemplateId = interfaceTemplateId;
    }

    public String getInterfaceTemplateName() {
        return interfaceTemplateName;
    }

    public void setInterfaceTemplateName(String interfaceTemplateName) {
        this.interfaceTemplateName = interfaceTemplateName == null ? null : interfaceTemplateName.trim();
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

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName == null ? null : nodeName.trim();
    }

    public String getNodeValue() {
        return nodeValue;
    }

    public void setNodeValue(String nodeValue) {
        this.nodeValue = nodeValue == null ? null : nodeValue.trim();
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath == null ? null : parentPath.trim();
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType == null ? null : nodeType.trim();
    }

    public Boolean getIsArrayItem() {
        return isArrayItem;
    }

    public void setIsArrayItem(Boolean isArrayItem) {
        this.isArrayItem = isArrayItem;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
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