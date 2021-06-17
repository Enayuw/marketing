package com.br.marketing.entity;

import java.util.Date;

public class CustomerInfoPushBatch {
    /**
     * 主键
     */
    private Long id;

    /**
     * 任务流水号
     */
    private Long mId;

    /**
     * 商户编号
     */
    private String mApiCode;

    /**
     * 批次号
     */
    private String mBatchNumber;

    /**
     * 客户批次号
     */
    private String mCusBatchNumber;

    /**
     * 跑分记录id
     */
    private Long mFileId;

    /**
     * 逻辑删除 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 更新记录时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getmId() {
        return mId;
    }

    public void setmId(Long mId) {
        this.mId = mId;
    }

    public String getmApiCode() {
        return mApiCode;
    }

    public void setmApiCode(String mApiCode) {
        this.mApiCode = mApiCode == null ? null : mApiCode.trim();
    }

    public String getmBatchNumber() {
        return mBatchNumber;
    }

    public void setmBatchNumber(String mBatchNumber) {
        this.mBatchNumber = mBatchNumber == null ? null : mBatchNumber.trim();
    }

    public String getmCusBatchNumber() {
        return mCusBatchNumber;
    }

    public void setmCusBatchNumber(String mCusBatchNumber) {
        this.mCusBatchNumber = mCusBatchNumber == null ? null : mCusBatchNumber.trim();
    }

    public Long getmFileId() {
        return mFileId;
    }

    public void setmFileId(Long mFileId) {
        this.mFileId = mFileId;
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