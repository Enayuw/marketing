package com.br.marketing.entity;

import java.util.Date;

public class DecisionsTaskLog {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 自动化决策配置id
     */
    private Long pushConfigId;

    /**
     * 决策任务id
     */
    private Long pushMainId;

    /**
     * 1-有效；9-无效
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

    public Long getPushConfigId() {
        return pushConfigId;
    }

    public void setPushConfigId(Long pushConfigId) {
        this.pushConfigId = pushConfigId;
    }

    public Long getPushMainId() {
        return pushMainId;
    }

    public void setPushMainId(Long pushMainId) {
        this.pushMainId = pushMainId;
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