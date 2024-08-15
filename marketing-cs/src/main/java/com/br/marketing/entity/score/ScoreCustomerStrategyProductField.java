package com.br.marketing.entity.score;

import java.util.Date;

public class ScoreCustomerStrategyProductField {
    /**
     *
     */
    private Long id;

    /**
     * 跑分产品字段主键
     */
    private Long scoreStrategyProductFieldId;

    /**
     * 客户信息主键
     */
    private Long marketingCustomerId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态：0-未删除；1-已删除
     */
    private Integer isDel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScoreStrategyProductFieldId() {
        return scoreStrategyProductFieldId;
    }

    public void setScoreStrategyProductFieldId(Long scoreStrategyProductFieldId) {
        this.scoreStrategyProductFieldId = scoreStrategyProductFieldId;
    }

    public Long getMarketingCustomerId() {
        return marketingCustomerId;
    }

    public void setMarketingCustomerId(Long marketingCustomerId) {
        this.marketingCustomerId = marketingCustomerId;
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

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }
}