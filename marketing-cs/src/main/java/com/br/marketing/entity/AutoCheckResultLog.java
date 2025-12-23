package com.br.marketing.entity;

import java.util.Date;

public class AutoCheckResultLog {
    /**
     * 
     */
    private Long id;

    /**
     * 用户编号
     */
    private String apiCode;

    /**
     * 场景编码(单场景)
     */
    private String sceneCode;

    /**
     * 前一天八点数据
     */
    private String lastData;

    /**
     * 本次数据
     */
    private String todayData;

    /**
     * 比对时间
     */
    private String compareTime;

    /**
     * 对比结果 0-不一致 1-有差异 2-一致
     */
    private String result;

    /**
     * 0-未删除 1-删除
     */
    private Byte isDeleted;

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

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode == null ? null : sceneCode.trim();
    }

    public String getLastData() {
        return lastData;
    }

    public void setLastData(String lastData) {
        this.lastData = lastData == null ? null : lastData.trim();
    }

    public String getTodayData() {
        return todayData;
    }

    public void setTodayData(String todayData) {
        this.todayData = todayData == null ? null : todayData.trim();
    }

    public String getCompareTime() {
        return compareTime;
    }

    public void setCompareTime(String compareTime) {
        this.compareTime = compareTime == null ? null : compareTime.trim();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result == null ? null : result.trim();
    }

    public Byte getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Byte isDeleted) {
        this.isDeleted = isDeleted;
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