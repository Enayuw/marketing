package com.br.marketing.entity;

import java.util.Date;

public class MockCase {
    /**
     * 
     */
    private Long id;

    /**
     * mock接口名称 枚举值维护
     */
    private String mockName;

    /**
     * mock用例名称
     */
    private String mockCaseName;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * Mock响应体
     */
    private String responseBody;

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 延迟毫秒数
     */
    private Integer delayMs;

    /**
     * 延迟波动（百分比）
     */
    private Integer delayFluctuation;

    /**
     * 描述
     */
    private String description;

    /**
     * 操作人id
     */
    private Long optUserId;

    /**
     * 操作人账户名
     */
    private String optUserName;

    /**
     * 是否启用 0-启动 1-关闭
     */
    private Integer enabled;

    /**
     * 创建日期
     */
    private String createDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMockName() {
        return mockName;
    }

    public void setMockName(String mockName) {
        this.mockName = mockName == null ? null : mockName.trim();
    }

    public String getMockCaseName() {
        return mockCaseName;
    }

    public void setMockCaseName(String mockCaseName) {
        this.mockCaseName = mockCaseName == null ? null : mockCaseName.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody == null ? null : responseBody.trim();
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(Integer delayMs) {
        this.delayMs = delayMs;
    }

    public Integer getDelayFluctuation() {
        return delayFluctuation;
    }

    public void setDelayFluctuation(Integer delayFluctuation) {
        this.delayFluctuation = delayFluctuation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Long getOptUserId() {
        return optUserId;
    }

    public void setOptUserId(Long optUserId) {
        this.optUserId = optUserId;
    }

    public String getOptUserName() {
        return optUserName;
    }

    public void setOptUserName(String optUserName) {
        this.optUserName = optUserName == null ? null : optUserName.trim();
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate == null ? null : createDate.trim();
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
}