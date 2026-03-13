package com.br.marketing.entity;

import java.util.Date;

/**
 * XYF 数据中转原始数据落库表
 */
public class XyfRelayRecord {

    private Long id;
    /** 客户编码 */
    private String corpCode;
    /** 请求批次编号（请求方批次唯一标识） */
    private String batchId;
    /** 批次日期 yyyymmdd */
    private String batchDate;
    /** 策略ID */
    private String strategyId;
    /** 原始数据 JSON（解密后的完整请求体） */
    private String rawData;
    /** 状态 0-接入中 1-接入成功 2-接入失败 */
    private Integer status;
    /** 错误信息 */
    private String msg;
    private Date createTime;
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCorpCode() {
        return corpCode;
    }

    public void setCorpCode(String corpCode) {
        this.corpCode = corpCode == null ? null : corpCode.trim();
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId == null ? null : batchId.trim();
    }

    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate == null ? null : batchDate.trim();
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId == null ? null : strategyId.trim();
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg == null ? null : msg.trim();
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
