package com.br.marketing.entity;

import java.util.Date;

public class ReportIntervalModel {
    /**
     * 
     */
    private Long id;

    /**
     * 自定义区间配置表id
     */
    private Long configId;

    /**
     * 模型类型：1-单模型，2-多模型
     */
    private String axisType;

    /**
     * X模型名称,多个[scorecashonxccrlr1,scorencashonxchx]
     */
    private String xModelName;

    /**
     * Y模型名称,多个[scorecashonxccrlr1,scorencashonxchx]
     */
    private String yModelName;

    /**
     * X区间配置JSON数组：[{"min":0,"max":50,"minInclusive":true,"maxInclusive":false,"text":"[0,50)"}]
     */
    private String xIntervalList;

    /**
     * Y区间配置JSON数组：[{"min":0,"max":50,"minInclusive":true,"maxInclusive":false,"text":"[0,50)"}]
     */
    private String yIntervalList;

    /**
     * 优先级
     */
    private String order;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getAxisType() {
        return axisType;
    }

    public void setAxisType(String axisType) {
        this.axisType = axisType == null ? null : axisType.trim();
    }

    public String getxModelName() {
        return xModelName;
    }

    public void setxModelName(String xModelName) {
        this.xModelName = xModelName == null ? null : xModelName.trim();
    }

    public String getyModelName() {
        return yModelName;
    }

    public void setyModelName(String yModelName) {
        this.yModelName = yModelName == null ? null : yModelName.trim();
    }

    public String getxIntervalList() {
        return xIntervalList;
    }

    public void setxIntervalList(String xIntervalList) {
        this.xIntervalList = xIntervalList == null ? null : xIntervalList.trim();
    }

    public String getyIntervalList() {
        return yIntervalList;
    }

    public void setyIntervalList(String yIntervalList) {
        this.yIntervalList = yIntervalList == null ? null : yIntervalList.trim();
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order == null ? null : order.trim();
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