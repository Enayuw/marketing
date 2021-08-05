package com.br.marketing.entity;

public class PushErrorLogWithBLOBs extends PushErrorLog {
    /**
     * 请求数据
     */
    private String requestStr;

    /**
     * 响应数据
     */
    private String responseStr;

    public String getRequestStr() {
        return requestStr;
    }

    public void setRequestStr(String requestStr) {
        this.requestStr = requestStr == null ? null : requestStr.trim();
    }

    public String getResponseStr() {
        return responseStr;
    }

    public void setResponseStr(String responseStr) {
        this.responseStr = responseStr == null ? null : responseStr.trim();
    }
}