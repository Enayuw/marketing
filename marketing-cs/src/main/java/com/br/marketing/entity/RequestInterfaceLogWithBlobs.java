package com.br.marketing.entity;

public class RequestInterfaceLogWithBlobs extends RequestInterfaceLog {
    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 返回结果
     */
    private String result;

    /**
     * 扩展信息
     */
    private String extendInfo;

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam == null ? null : requestParam.trim();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result == null ? null : result.trim();
    }

    public String getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(String extendInfo) {
        this.extendInfo = extendInfo == null ? null : extendInfo.trim();
    }
}