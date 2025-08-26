package com.br.marketing.entity;

import java.util.Date;

public class AiToPolicyRecord {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 数据指纹，数据唯一标识
     */
    private Long fingerprint;

    /**
     * APICODE
     */
    private String apiCode;

    /**
     * 客户规则标签
     */
    private String ruleLabel;

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
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(Long fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getRuleLabel() {
        return ruleLabel;
    }

    public void setRuleLabel(String ruleLabel) {
        this.ruleLabel = ruleLabel == null ? null : ruleLabel.trim();
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