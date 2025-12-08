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
     * 重试数据包id集合
     */
    private String retryPackageIds;

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
     * 撞库量级上限
     */
    private Integer limitNum;

    /**
     * 剔除规则id集合
     */
    private String deleteRuleIds;

    /**
     * 剔除量级
     */
    private Integer deleteNum;

    /**
     * 补充规则信息
     */
    private String supplyRuleInfo;

    /**
     * 补充数据包id
     */
    private String supplyPackageId;

    /**
     * 重试补充fail_msg集合
     */
    private String retrySupplyFailMsgs;

    /**
     * 补充量级
     */
    private Integer supplyNum;

    /**
     * 推送时间
     */
    private Date pushTime;

    /**
     * 推送量级
     */
    private Integer pushNum;

    /**
     * 任务状态 0-待统计；1-统计中；2-统计完成；3-筛选中；4-筛选完成；5-推送中；6-推送完成；7-推送失败；
     */
    private Integer status;

    /**
     * 重新筛选
     */
    private Integer isretry;

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

    public String getRetryPackageIds() {
        return retryPackageIds;
    }

    public void setRetryPackageIds(String retryPackageIds) {
        this.retryPackageIds = retryPackageIds == null ? null : retryPackageIds.trim();
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

    public Integer getLimitNum() {
        return limitNum;
    }

    public void setLimitNum(Integer limitNum) {
        this.limitNum = limitNum;
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

    public String getSupplyRuleInfo() {
        return supplyRuleInfo;
    }

    public void setSupplyRuleInfo(String supplyRuleInfo) {
        this.supplyRuleInfo = supplyRuleInfo == null ? null : supplyRuleInfo.trim();
    }

    public String getSupplyPackageId() {
        return supplyPackageId;
    }

    public void setSupplyPackageId(String supplyPackageId) {
        this.supplyPackageId = supplyPackageId == null ? null : supplyPackageId.trim();
    }

    public String getRetrySupplyFailMsgs() {
        return retrySupplyFailMsgs;
    }

    public void setRetrySupplyFailMsgs(String retrySupplyFailMsgs) {
        this.retrySupplyFailMsgs = retrySupplyFailMsgs == null ? null : retrySupplyFailMsgs.trim();
    }

    public Integer getSupplyNum() {
        return supplyNum;
    }

    public void setSupplyNum(Integer supplyNum) {
        this.supplyNum = supplyNum;
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
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

    public Integer getIsretry() {
        return isretry;
    }

    public void setIsretry(Integer isretry) {
        this.isretry = isretry;
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