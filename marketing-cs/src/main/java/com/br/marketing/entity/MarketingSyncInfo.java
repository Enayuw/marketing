package com.br.marketing.entity;

import java.util.Date;

public class MarketingSyncInfo {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 客户批次号
     */
    private String cusBatch;

    /**
     * 请求批次号
     */
    private String requestBatch;

    /**
     * 
     */
    private Byte last;

    /**
     * 
     */
    private Long total;

    /**
     * 状态 1-进行中；2-全部成功；3-全部失败；4-部分成功
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 错误id
     */
    private Long errorId;

    /**
     * 数据实际条数
     */
    private Integer actualNum;

    /**
     * 1未同步；2已同步
     */
    private Integer isUpload;

    /**
     * 数据
     */
    private String jsonData;

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

    public String getCusBatch() {
        return cusBatch;
    }

    public void setCusBatch(String cusBatch) {
        this.cusBatch = cusBatch == null ? null : cusBatch.trim();
    }

    public String getRequestBatch() {
        return requestBatch;
    }

    public void setRequestBatch(String requestBatch) {
        this.requestBatch = requestBatch == null ? null : requestBatch.trim();
    }

    public Byte getLast() {
        return last;
    }

    public void setLast(Byte last) {
        this.last = last;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public Long getErrorId() {
        return errorId;
    }

    public void setErrorId(Long errorId) {
        this.errorId = errorId;
    }

    public Integer getActualNum() {
        return actualNum;
    }

    public void setActualNum(Integer actualNum) {
        this.actualNum = actualNum;
    }

    public Integer getIsUpload() {
        return isUpload;
    }

    public void setIsUpload(Integer isUpload) {
        this.isUpload = isUpload;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData == null ? null : jsonData.trim();
    }
}