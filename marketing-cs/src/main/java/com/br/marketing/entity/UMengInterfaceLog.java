package com.br.marketing.entity;

import java.util.Date;

public class UMengInterfaceLog {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 请求类型 1:创建时机任务 2:设备注册 3:友盟回调
     */
    private Integer requestType;

    /**
     * 友盟回调的event_type 21:设备注册成功  22:设备注册失败 1001:智能时机回调
     */
    private String eventType;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 方法名称
     */
    private String url;

    /**
     * header信息
     */
    private String header;

    /**
     * 返回结果
     */
    private String result;

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
    private String expire;

    /**
     * 扩展信息
     */
    private String extendInfo;

    /**
     * 入库时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * phone_sha256 
     */
    private String phoneSha256;

    /**
     * 友盟原始加密请求参数
     */
    private String encryptParam;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLocalId() {
        return localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public Integer getRequestType() {
        return requestType;
    }

    public void setRequestType(Integer requestType) {
        this.requestType = requestType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType == null ? null : eventType.trim();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam == null ? null : requestParam.trim();
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result == null ? null : result.trim();
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

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire == null ? null : expire.trim();
    }

    public String getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo == null ? null : extendInfo.trim();
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

    public String getPhoneSha256() {
        return phoneSha256;
    }

    public void setPhoneSha256(String phoneSha256) {
        this.phoneSha256 = phoneSha256 == null ? null : phoneSha256.trim();
    }

    public String getEncryptParam() {
        return encryptParam;
    }

    public void setEncryptParam(String encryptParam) {
        this.encryptParam = encryptParam == null ? null : encryptParam.trim();
    }
}