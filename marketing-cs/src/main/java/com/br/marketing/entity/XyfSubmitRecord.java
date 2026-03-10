package com.br.marketing.entity;

import java.util.Date;

public class XyfSubmitRecord {
    /**
     * 主键
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 请求批次编号
     */
    private String batchId;

    /**
     * 批次日期
     */
    private String batchDate;

    /**
     * 客户编码
     */
    private String corpCode;

    /**
     * 授权码
     */
    private String accessToken;

    /**
     * 策略ID
     */
    private String strategyId;

    /**
     * 原始数据
     */
    private String originData;

    /**
     * 明文数据
     */
    private String plainData;

    /**
     * 接收状态 0-未解密；1-未验签；2-未必填；3-接收成功；
     */
    private Integer receiveStatus;

    /**
     * 上传状态 0-未上传；1-上传中；2-上传成功；3-上传失败；
     */
    private Integer syncStatus;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 删除状态 1-可用 9-删除
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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
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

    public String getCorpCode() {
        return corpCode;
    }

    public void setCorpCode(String corpCode) {
        this.corpCode = corpCode == null ? null : corpCode.trim();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken == null ? null : accessToken.trim();
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId == null ? null : strategyId.trim();
    }

    public String getOriginData() {
        return originData;
    }

    public void setOriginData(String originData) {
        this.originData = originData == null ? null : originData.trim();
    }

    public String getPlainData() {
        return plainData;
    }

    public void setPlainData(String plainData) {
        this.plainData = plainData == null ? null : plainData.trim();
    }

    public Integer getReceiveStatus() {
        return receiveStatus;
    }

    public void setReceiveStatus(Integer receiveStatus) {
        this.receiveStatus = receiveStatus;
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
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