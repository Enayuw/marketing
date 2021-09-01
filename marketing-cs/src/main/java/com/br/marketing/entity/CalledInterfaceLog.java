package com.br.marketing.entity;

import java.util.Date;

public class CalledInterfaceLog {
    /**
     * 自增主键
     */
    private Long id;

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
    private String methodName;

    /**
     * 返回结果
     */
    private String result;

    /**
     * 成功标识1-成功；2-失败
     */
    private Integer code;

    /**
     * 耗时
     */
    private String expire;

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

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam == null ? null : requestParam.trim();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName == null ? null : methodName.trim();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result == null ? null : result.trim();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire == null ? null : expire.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}