package com.br.marketing.entity;

public class DwsXcDataRatioD {
    /**
     * 日期
     */
    private String reportDate;

    /**
     * 撞得量
     */
    private Integer collidingBackNum;

    /**
     * 析出量
     */
    private Integer extractionNum;

    /**
     * 可外呼量
     */
    private Integer callableNum;

    /**
     * 析出率
     */
    private Object extractionRatio;

    /**
     * 可外呼率
     */
    private Object callableRatio;

    /**
     * 
     */
    private Object createTime;

    /**
     * 
     */
    private Object updateTime;

    /**
     * 状态 0-正常1 删除
     */
    private Integer isDelete;

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate == null ? null : reportDate.trim();
    }

    public Integer getCollidingBackNum() {
        return collidingBackNum;
    }

    public void setCollidingBackNum(Integer collidingBackNum) {
        this.collidingBackNum = collidingBackNum;
    }

    public Integer getExtractionNum() {
        return extractionNum;
    }

    public void setExtractionNum(Integer extractionNum) {
        this.extractionNum = extractionNum;
    }

    public Integer getCallableNum() {
        return callableNum;
    }

    public void setCallableNum(Integer callableNum) {
        this.callableNum = callableNum;
    }

    public Object getExtractionRatio() {
        return extractionRatio;
    }

    public void setExtractionRatio(Object extractionRatio) {
        this.extractionRatio = extractionRatio;
    }

    public Object getCallableRatio() {
        return callableRatio;
    }

    public void setCallableRatio(Object callableRatio) {
        this.callableRatio = callableRatio;
    }

    public Object getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Object createTime) {
        this.createTime = createTime;
    }

    public Object getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Object updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}