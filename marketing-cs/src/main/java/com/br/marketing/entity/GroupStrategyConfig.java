package com.br.marketing.entity;

import java.util.Date;

public class GroupStrategyConfig {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 客户商号
     */
    private String apiCode;

    /**
     * 场景
     */
    private String groupType;

    /**
     * 策略
     */
    private String strategyId;

    /**
     * 是否有效1-有效；9-无效；
     */
    private Integer isDel;

    /**
     * 入库时间
     */
    private Date createTime;

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

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType == null ? null : groupType.trim();
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId == null ? null : strategyId.trim();
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
}