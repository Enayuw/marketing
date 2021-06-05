package com.br.marketing.common.constants.web;

/**
 * 策略请求类型，
 * 策略请求的调用类型
 */
public enum RequestType {
//web单条
    WEB_SIGNAL("controller","web单条"),
    //web批量
    WEB_BATCH("web_batch","web批量"),
    //api
    API("api","api"),
    //api批量
    API_BATCH("api_batch","api批量"),
            ;
    private final String code;
    private final String message;

    private RequestType(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


}
