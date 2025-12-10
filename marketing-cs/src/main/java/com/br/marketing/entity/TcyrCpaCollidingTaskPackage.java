package com.br.marketing.entity;

import java.util.Date;

public class TcyrCpaCollidingTaskPackage {
    /**
     * 
     */
    private Long id;

    /**
     * 撞库任务id
     */
    private Long collidingTaskId;

    /**
     * 包类型 1-数据包；2-补充包
     */
    private Integer packageType;

    /**
     * 数据包id
     */
    private Long packageId;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 失败类型 1-黑名单；2-已被锁定；3-已转化；4-无此用户；5-超出限额；6-空白组
     */
    private String failMsg;

    /**
     * 补充规则信息
     */
    private String supplyRuleInfo;

    /**
     * 执行sql
     */
    private String executeSql;

    /**
     * 任务状态 0-待执行；1-执行中；2-执行完成
     */
    private Integer status;

    /**
     * 量级
     */
    private Integer magnitude;

    /**
     * 删除状态 1-可用 9-删除
     */
    private Integer isDel;

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

    public Long getCollidingTaskId() {
        return collidingTaskId;
    }

    public void setCollidingTaskId(Long collidingTaskId) {
        this.collidingTaskId = collidingTaskId;
    }

    public Integer getPackageType() {
        return packageType;
    }

    public void setPackageType(Integer packageType) {
        this.packageType = packageType;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getFailMsg() {
        return failMsg;
    }

    public void setFailMsg(String failMsg) {
        this.failMsg = failMsg == null ? null : failMsg.trim();
    }

    public String getSupplyRuleInfo() {
        return supplyRuleInfo;
    }

    public void setSupplyRuleInfo(String supplyRuleInfo) {
        this.supplyRuleInfo = supplyRuleInfo == null ? null : supplyRuleInfo.trim();
    }

    public String getExecuteSql() {
        return executeSql;
    }

    public void setExecuteSql(String executeSql) {
        this.executeSql = executeSql == null ? null : executeSql.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Integer magnitude) {
        this.magnitude = magnitude;
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
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