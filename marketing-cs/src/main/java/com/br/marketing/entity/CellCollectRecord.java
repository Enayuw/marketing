package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class CellCollectRecord {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * apiCode维度任务同步上传表的最大id
     */
    private Long uploadMaxId;

    /**
     * apiCode维度同步任务是否已开始
     */
    private Integer status;

    /**
     * apiCode维度已请求云客的最大id
     */
    private Long maxId;

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

    public Long getUploadMaxId() {
        return uploadMaxId;
    }

    public void setUploadMaxId(Long uploadMaxId) {
        this.uploadMaxId = uploadMaxId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getMaxId() {
        return maxId;
    }

    public void setMaxId(Long maxId) {
        this.maxId = maxId;
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