package com.br.marketing.entity;

import java.util.Date;

/**
 * 短链转化规则实体类
 * @author system
 * @date 2025/01/17
 */
public class ShortLinkTransferRule {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 规则编码
     */
    private String linkRuleCode;

    /**
     * 规则名称
     */
    private String linkRuleName;

    /**
     * API编码
     */
    private String apiCode;

    /**
     * 状态:1-启用,0-禁用
     */
    private Byte status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLinkRuleCode() {
        return linkRuleCode;
    }

    public void setLinkRuleCode(String linkRuleCode) {
        this.linkRuleCode = linkRuleCode == null ? null : linkRuleCode.trim();
    }

    public String getLinkRuleName() {
        return linkRuleName;
    }

    public void setLinkRuleName(String linkRuleName) {
        this.linkRuleName = linkRuleName == null ? null : linkRuleName.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
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

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy == null ? null : createBy.trim();
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy == null ? null : updateBy.trim();
    }
}
