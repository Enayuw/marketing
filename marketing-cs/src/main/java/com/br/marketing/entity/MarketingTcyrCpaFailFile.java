package com.br.marketing.entity;

import java.util.Date;

public class MarketingTcyrCpaFailFile {
    /**
     * 
     */
    private Long id;

    /**
     * 同程请求的记录id
     */
    private Long syncRecordId;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * txt文件名称
     */
    private String fileName;

    /**
     * txt的文件目录
     */
    private String filePath;

    /**
     * txt文件总行数
     */
    private Long totalCount;

    /**
     * 入库成功的总条数
     */
    private Long successCount;

    /**
     * 文件数据处理流程状态  0-未处理 1-处理中 2-处理完成,3-处理异常
     */
    private Integer collidingDataDealStatus;

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

    /**
     * 扩展字段
     */
    private String extend;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSyncRecordId() {
        return syncRecordId;
    }

    public void setSyncRecordId(Long syncRecordId) {
        this.syncRecordId = syncRecordId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo == null ? null : batchNo.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? null : filePath.trim();
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Integer getCollidingDataDealStatus() {
        return collidingDataDealStatus;
    }

    public void setCollidingDataDealStatus(Integer collidingDataDealStatus) {
        this.collidingDataDealStatus = collidingDataDealStatus;
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

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend == null ? null : extend.trim();
    }
}