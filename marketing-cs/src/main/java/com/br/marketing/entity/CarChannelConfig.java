package com.br.marketing.entity;

import java.util.Date;

public class CarChannelConfig {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String cid;

    /**
     * 
     */
    private String apiCode;

    /**
     * 渠道商名称
     */
    private String name;

    /**
     * 渠道商优先级
     */
    private Integer order;

    /**
     * 渠道商配置更新实现
     */
    private String strategyConfigInfo;

    /**
     * 线索过滤实现
     */
    private String strategyFitler;

    /**
     * 线索匹配实现
     */
    private String strategyMatch;

    /**
     * 线索推送实现
     */
    private String strategyPush;

    /**
     * 线索回调实现
     */
    private String strategyCallback;

    /**
     * 1-有效；2-无效
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

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order == null ? null : order;
    }

    public String getStrategyConfigInfo() {
        return strategyConfigInfo;
    }

    public void setStrategyConfigInfo(String strategyConfigInfo) {
        this.strategyConfigInfo = strategyConfigInfo == null ? null : strategyConfigInfo.trim();
    }

    public String getStrategyFitler() {
        return strategyFitler;
    }

    public void setStrategyFitler(String strategyFitler) {
        this.strategyFitler = strategyFitler == null ? null : strategyFitler.trim();
    }

    public String getStrategyMatch() {
        return strategyMatch;
    }

    public void setStrategyMatch(String strategyMatch) {
        this.strategyMatch = strategyMatch == null ? null : strategyMatch.trim();
    }

    public String getStrategyPush() {
        return strategyPush;
    }

    public void setStrategyPush(String strategyPush) {
        this.strategyPush = strategyPush == null ? null : strategyPush.trim();
    }

    public String getStrategyCallback() {
        return strategyCallback;
    }

    public void setStrategyCallback(String strategyCallback) {
        this.strategyCallback = strategyCallback == null ? null : strategyCallback.trim();
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