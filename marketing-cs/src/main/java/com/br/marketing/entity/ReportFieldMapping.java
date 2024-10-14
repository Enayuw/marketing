package com.br.marketing.entity;

import java.util.Date;

public class ReportFieldMapping {
    /**
     * id
     */
    private Long id;

    /**
     * 报表任务id
     */
    private String reportTaskId;

    /**
     * 场景
     */
    private String userType;

    /**
     * 展示列名
     */
    private String itemShow;

    /**
     * 指标列
     */
    private String itemName;

    /**
     * 展示格式
     */
    private String itemFormatType;

    /**
     * 报表列顺序
     */
    private Integer itemOrder;

    /**
     * 是否有效 1-有效；9-失效
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

    public String getReportTaskId() {
        return reportTaskId;
    }

    public void setReportTaskId(String reportTaskId) {
        this.reportTaskId = reportTaskId == null ? null : reportTaskId.trim();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getItemShow() {
        return itemShow;
    }

    public void setItemShow(String itemShow) {
        this.itemShow = itemShow == null ? null : itemShow.trim();
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName == null ? null : itemName.trim();
    }

    public String getItemFormatType() {
        return itemFormatType;
    }

    public void setItemFormatType(String itemFormatType) {
        this.itemFormatType = itemFormatType == null ? null : itemFormatType.trim();
    }

    public Integer getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(Integer itemOrder) {
        this.itemOrder = itemOrder;
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