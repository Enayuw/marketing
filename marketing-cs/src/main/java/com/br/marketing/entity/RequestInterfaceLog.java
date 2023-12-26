package com.br.marketing.entity;

import java.util.Date;

public class RequestInterfaceLog {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 方法名称
     */
    private String url;

    /**
     * header信息
     */
    private String header;

    /**
     * httpcode
     */
    private Integer httpCode;

    /**
     * 调用次数
     */
    private Integer callTime;

    /**
     * 耗时
     */
    private Long expire;

    /**
     * 入库时间
     */
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header == null ? null : header.trim();
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
    }

    public Integer getCallTime() {
        return callTime;
    }

    public void setCallTime(Integer callTime) {
        this.callTime = callTime;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}