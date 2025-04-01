package com.br.marketing.entity;

import java.util.Date;

public class WubaCollidingConfig {
    /**
     * 
     */
    private Long id;

    /**
     * 优先级(1~100，值越小，优先级越高)
     */
    private Integer priority;

    /**
     * 数据来源类型：T-非金融周期，S-金融周期，H-高价值，F-手动补包，J-非金融周期-2，Q-金融周期-2，K-补包-2
     */
    private String dataSourceType;

    /**
     * 跑分类型：top-TOP，medium-下探1，down-下探2，low-LOW
     */
    private String scoreType;

    /**
     * 查询数据sql
     */
    private String querySql;

    /**
     * 查询逻辑说明
     */
    private String queryDesc;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDeleted;

    /**
     * 扩展字段
     */
    private String extend;

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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType == null ? null : dataSourceType.trim();
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType == null ? null : scoreType.trim();
    }

    public String getQuerySql() {
        return querySql;
    }

    public void setQuerySql(String querySql) {
        this.querySql = querySql == null ? null : querySql.trim();
    }

    public String getQueryDesc() {
        return queryDesc;
    }

    public void setQueryDesc(String queryDesc) {
        this.queryDesc = queryDesc == null ? null : queryDesc.trim();
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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