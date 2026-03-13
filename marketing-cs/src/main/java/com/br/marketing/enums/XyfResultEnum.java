package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum XyfResultEnum {

    OK("1","0000000", "OK"),
    BAD_REQUEST("0", "1010000", "系统异常"),
    BATCH_NO_ERROR("0", "101005", "任务批次号有误"),
    REQUIRED_PARAMETER_EMPTY("0", "1010006", "必填参数为空"),
    REQUIRED_PARAMETER_ERROR("0", "1010007", "参数范围有误"),
    BAD_REQUEST_READ_TIME_OUT("0", "1010008", "Read timed out"),
    REQUEST_ERROR("0", "1050001", "请求异常"),
    INVALID_HEADER("0","1010201","请求头参数缺失"),
    INVALID_TIMESTAMP("0","1010202","请求头时间戳参数过期"),
    INVALID_SIGN("0","1010203","sign error"),
    APP_KEY_NOT_EXIST("0","1010204","appKey不存在！"),
    APP_KEY_ERROR("0", "1010106", "APP_KEY有误"),
    VERIFY_TOKEN_ERROR("0", "1010107", "验证token失败，请重新调用验证平台权限接口"),
    TIMESTAMP_MISSING_ERROR("0", "1010108", "请求时间戳缺失"),
    REQUEST_TIME_TOO_LONG_ERROR("0", "1010109", "请求时间太长"),
    LOGIN_INFORMATION_IS_INVALID("0", "10101010", "用户token失效，请重新验证用户权限"),
    TOKEN_MISSING_ERROR("0", "10101011", "token参数缺失"),
    PARAMETERS_DECRYPTION_ERROR("0", "10101012", "请求参数解密失败，请重新传参"),
    PARAMETERS_MISSING_ERROR("0", "10101013", "必传参数缺失");

    private String status;

    private String error;

    private String msg;

    XyfResultEnum(String status, String error, String msg) {
        this.status = status;
        this.error = error;
        this.msg = msg;
    }
}
