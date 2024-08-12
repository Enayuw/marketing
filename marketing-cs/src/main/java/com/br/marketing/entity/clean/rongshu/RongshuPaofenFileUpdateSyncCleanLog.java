package com.br.marketing.entity.clean.rongshu;

import java.util.Date;

public class RongshuPaofenFileUpdateSyncCleanLog {
    /**
     *
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 数据清洗文件id
     */
    private Long marketingCleanDataFileId;

    /**
     * 清洗的上传数据id
     */
    private Long syncApicodeId;

    /**
     * 历史数据内容，json结构
     */
    private String historyDataJson;

    /**
     * 条件
     */
    private String uid;

    /**
     * 清洗状态：0-已成功；1-失败
     */
    private Integer isSuccess;

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

    /**
     * 历史数据内容，json结构
     */
    private String newDataJson;

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

    public Long getMarketingCleanDataFileId() {
        return marketingCleanDataFileId;
    }

    public void setMarketingCleanDataFileId(Long marketingCleanDataFileId) {
        this.marketingCleanDataFileId = marketingCleanDataFileId;
    }

    public Long getSyncApicodeId() {
        return syncApicodeId;
    }

    public void setSyncApicodeId(Long syncApicodeId) {
        this.syncApicodeId = syncApicodeId;
    }

    public String getHistoryDataJson() {
        return historyDataJson;
    }

    public void setHistoryDataJson(String historyDataJson) {
        this.historyDataJson = historyDataJson == null ? null : historyDataJson.trim();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public Integer getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Integer isSuccess) {
        this.isSuccess = isSuccess;
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

    public String getNewDataJson() {
        return newDataJson;
    }

    public void setNewDataJson(String newDataJson) {
        this.newDataJson = newDataJson == null ? null : newDataJson.trim();
    }
}