package com.br.marketing.entity;

import java.util.Date;

public class StrategyProductConfig {
    /**
     * 主键
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 用户的基本信息 字段间用逗号分隔
     */
    private String baseInfo;

    /**
     * 策略产品配置信息
     */
    private String strategyProductJson;

    /**
     * 删除标志 1-有效；9-删除
     */
    private Integer isDel;

    /**
     * 创建时间
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

    public String getBaseInfo() {
        return baseInfo;
    }

    public void setBaseInfo(String baseInfo) {
        this.baseInfo = baseInfo == null ? null : baseInfo.trim();
    }

    public String getStrategyProductJson() {
        return strategyProductJson;
    }

    public void setStrategyProductJson(String strategyProductJson) {
        this.strategyProductJson = strategyProductJson == null ? null : strategyProductJson.trim();
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