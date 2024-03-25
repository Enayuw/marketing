package com.br.marketing.entity;

import java.util.Date;

public class XieChengCollidingDataContrast {
    /**
     * 
     */
    private Long id;

    /**
     * 规则条件 1 ，2，,3
     */
    private Integer ruleTypeFlag;

    /**
     * 包id
     */
    private Long packageId;

    /**
     * 
     */
    private String batchNumber;

    /**
     * 手机号
     */
    private String cellSha256CodeList;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 扩展字段
     */
    private String extend;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRuleTypeFlag() {
        return ruleTypeFlag;
    }

    public void setRuleTypeFlag(Integer ruleTypeFlag) {
        this.ruleTypeFlag = ruleTypeFlag;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber == null ? null : batchNumber.trim();
    }

    public String getCellSha256CodeList() {
        return cellSha256CodeList;
    }

    public void setCellSha256CodeList(String cellSha256CodeList) {
        this.cellSha256CodeList = cellSha256CodeList == null ? null : cellSha256CodeList.trim();
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
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

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}