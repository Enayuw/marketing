package com.br.marketing.entity;

import java.util.Date;

public class ClueRelationship {
    /**
     * 
     */
    private Long id;

    /**
     * 线索id
     */
    private Long clueInfoId;

    /**
     * 
     */
    private String apiCode;

    /**
     * 外采映射id
     */
    private Long mappingId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 状态 0-有效 1-失效
     */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClueInfoId() {
        return clueInfoId;
    }

    public void setClueInfoId(Long clueInfoId) {
        this.clueInfoId = clueInfoId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Long getMappingId() {
        return mappingId;
    }

    public void setMappingId(Long mappingId) {
        this.mappingId = mappingId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}