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
     * 场景简拼 用于批次号生成使用
     */
    private String groupTypeShort;

    /**
     * 策略
     */
    private String strategyId;

    /**
     * 返回用户基本字段表头 字段用逗号分隔
     */
    private String baseInfo;

    /**
     * 是否有效1-有效；9-无效；
     */
    private Integer isDel;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 任务执行策略 1-一次性全量；2-周期性全量
     */
    private Integer execType;

    /**
     * 周期天数
     */
    private Integer cycleDay;

    /**
     * 周期结束时间
     */
    private String cycleEndDay;

    /**
     * 推送客户类型  0 文件，1 api  默认支持文件推送
     */
    private Integer pushType;

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

    public String getGroupTypeShort() {
        return groupTypeShort;
    }

    public void setGroupTypeShort(String groupTypeShort) {
        this.groupTypeShort = groupTypeShort == null ? null : groupTypeShort.trim();
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId == null ? null : strategyId.trim();
    }

    public String getBaseInfo() {
        return baseInfo;
    }

    public void setBaseInfo(String baseInfo) {
        this.baseInfo = baseInfo == null ? null : baseInfo.trim();
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

    public Integer getExecType() {
        return execType;
    }

    public void setExecType(Integer execType) {
        this.execType = execType;
    }

    public Integer getCycleDay() {
        return cycleDay;
    }

    public void setCycleDay(Integer cycleDay) {
        this.cycleDay = cycleDay;
    }

    public String getCycleEndDay() {
        return cycleEndDay;
    }

    public void setCycleEndDay(String cycleEndDay) {
        this.cycleEndDay = cycleEndDay == null ? null : cycleEndDay.trim();
    }

    public Integer getPushType() {
        return pushType;
    }

    public void setPushType(Integer pushType) {
        this.pushType = pushType;
    }
}