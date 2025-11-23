package com.br.marketing.datarelayservice.enums;

import lombok.Getter;

/**
 * 中原消金响应码枚举
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Getter
public enum ZhongYuanResponseCodeEnum {

    /**
     * 成功
     */
    SUCCESS("0000000", "操作成功"),

    /**
     * 参数错误
     */
    PARAM_ERROR("1000001", "参数错误"),

    /**
     * Token无效
     */
    TOKEN_INVALID("1000002", "Token无效"),

    /**
     * 系统异常
     */
    SYSTEM_ERROR("1000003", "系统异常"),

    /**
     * 部分成功（批量操作时部分成功）
     */
    PARTIAL_SUCCESS("1111111", "部分成功"),

    /**
     * 全部失败（批量操作时全部失败）
     */
    ALL_FAILED("2222222", "全部失败");

    /**
     * 响应码
     */
    private final String code;

    /**
     * 响应消息
     */
    private final String message;

    ZhongYuanResponseCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

