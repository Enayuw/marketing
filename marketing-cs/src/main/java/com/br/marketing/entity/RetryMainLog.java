package com.br.marketing.entity;

import java.util.Date;

public class RetryMainLog {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 发号自增id
     */
    private Long incrId;

    /**
     * 重试类型 1-方法重试；2-接口重试
     */
    private Integer retryType;

    /**
     * 重试参数
     */
    private String retryParam;

    /**
     * 重试参数类名称
     */
    private String retryParamType;

    /**
     * 重试服务名
     */
    private String retryService;

    /**
     * 重试方法名称
     */
    private String retryMethod;

    /**
     * 重试次数
     */
    private Integer retryNum;

    /**
     * 重试最大次数
     */
    private Integer retryMaxNum;

    /**
     * 重试状态 1-重试中；2-重试成功；3-重试失败
     */
    private Integer retryStatus;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 重试service的实现类型1-aop
     */
    private Integer serviceType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIncrId() {
        return incrId;
    }

    public void setIncrId(Long incrId) {
        this.incrId = incrId;
    }

    public Integer getRetryType() {
        return retryType;
    }

    public void setRetryType(Integer retryType) {
        this.retryType = retryType;
    }

    public String getRetryParam() {
        return retryParam;
    }

    public void setRetryParam(String retryParam) {
        this.retryParam = retryParam == null ? null : retryParam.trim();
    }

    public String getRetryParamType() {
        return retryParamType;
    }

    public void setRetryParamType(String retryParamType) {
        this.retryParamType = retryParamType == null ? null : retryParamType.trim();
    }

    public String getRetryService() {
        return retryService;
    }

    public void setRetryService(String retryService) {
        this.retryService = retryService == null ? null : retryService.trim();
    }

    public String getRetryMethod() {
        return retryMethod;
    }

    public void setRetryMethod(String retryMethod) {
        this.retryMethod = retryMethod == null ? null : retryMethod.trim();
    }

    public Integer getRetryNum() {
        return retryNum;
    }

    public void setRetryNum(Integer retryNum) {
        this.retryNum = retryNum;
    }

    public Integer getRetryMaxNum() {
        return retryMaxNum;
    }

    public void setRetryMaxNum(Integer retryMaxNum) {
        this.retryMaxNum = retryMaxNum;
    }

    public Integer getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(Integer retryStatus) {
        this.retryStatus = retryStatus;
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

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }
}