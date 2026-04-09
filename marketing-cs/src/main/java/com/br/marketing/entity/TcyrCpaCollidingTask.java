package com.br.marketing.entity;

import java.util.Date;

public class TcyrCpaCollidingTask {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 数据包id集合
     */
    private String packageIds;

    /**
     * 数据包名称集合
     */
    private String packageNames;

    /**
     * 撞库日期
     */
    private Date collidingDate;

    /**
     * 撞库时间
     */
    private Date collidingTime;

    /**
     * 撞库量级
     */
    private Integer collidingNum;

    /**
     * 剔除规则id集合
     */
    private String deleteRuleIds;

    /**
     * 剔除量级
     */
    private Integer deleteNum;

    /**
     * 剔除详情
     */
    private String deleteInfo;

    /**
     * 补充规则信息
     */
    private String supplyRuleInfo;

    /**
     * 补充量级
     */
    private Integer supplyNum;

    /**
     * 预估量级
     */
    private Integer estNum;

    /**
     * 推送量级
     */
    private Integer pushNum;

    /**
     * 任务状态 1-待统计；2-统计完成；3-筛选中；4-筛选完成；5-推送中；6-推送完成；7-推送失败；
     */
    private Integer status;

    /**
     * 禁用标志 0-禁用 1-启用
     */
    private Integer enabled;

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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getPackageIds() {
        return packageIds;
    }

    public void setPackageIds(String packageIds) {
        this.packageIds = packageIds == null ? null : packageIds.trim();
    }

    public String getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(String packageNames) {
        this.packageNames = packageNames == null ? null : packageNames.trim();
    }

    public Date getCollidingDate() {
        return collidingDate;
    }

    public void setCollidingDate(Date collidingDate) {
        this.collidingDate = collidingDate;
    }

    public Date getCollidingTime() {
        return collidingTime;
    }

    public void setCollidingTime(Date collidingTime) {
        this.collidingTime = collidingTime;
    }

    public Integer getCollidingNum() {
        return collidingNum;
    }

    public void setCollidingNum(Integer collidingNum) {
        this.collidingNum = collidingNum;
    }

    public String getDeleteRuleIds() {
        return deleteRuleIds;
    }

    public void setDeleteRuleIds(String deleteRuleIds) {
        this.deleteRuleIds = deleteRuleIds == null ? null : deleteRuleIds.trim();
    }

    public Integer getDeleteNum() {
        return deleteNum;
    }

    public void setDeleteNum(Integer deleteNum) {
        this.deleteNum = deleteNum;
    }

    public String getDeleteInfo() {
        return deleteInfo;
    }

    public void setDeleteInfo(String deleteInfo) {
        this.deleteInfo = deleteInfo == null ? null : deleteInfo.trim();
    }

    public String getSupplyRuleInfo() {
        return supplyRuleInfo;
    }

    public void setSupplyRuleInfo(String supplyRuleInfo) {
        this.supplyRuleInfo = supplyRuleInfo == null ? null : supplyRuleInfo.trim();
    }

    public Integer getSupplyNum() {
        return supplyNum;
    }

    public void setSupplyNum(Integer supplyNum) {
        this.supplyNum = supplyNum;
    }

    public Integer getEstNum() {
        return estNum;
    }

    public void setEstNum(Integer estNum) {
        this.estNum = estNum;
    }

    public Integer getPushNum() {
        return pushNum;
    }

    public void setPushNum(Integer pushNum) {
        this.pushNum = pushNum;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
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