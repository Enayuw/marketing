package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 画像异常信息枚举
 *
 * @author zhen.Li1
 * @dateTime 2024/4/03 16:00
 */
@Getter
@AllArgsConstructor
public enum HxResultErrorCodeEnum {

    SYSTEM_ERROR("100001","程序错误，系统异常"),
    KEY_ISEMPTY("100003","必选key值缺失或不合法"),
    PARAM_ERROR("100006","请求参数格式错误"),
    APICODE_NO_PRIVILEGE("100008","apiCode无权限"),
    OVER_NUM_ERROR("100010","访问次数超限"),
    ACCOUNT_STOP("100011","账户停用"),
    PACKAGE_IS_EMPTY("100012","请求套餐为空"),
    JSON_ERROR("1000016","捕获请求json异常，无法解析的错误"),

    FLAG_SYSTEM_ERROR("99","系统异常"),
    FLAG_KEY_ERROR("98","入参key缺失"),
    FLAG_TIME_OVER_ERROR("98","产品访问次数超限"),
    ;
    /**
     * 状态码
     */
    private String code;

    /**
     * 信息
     */
    private String message;

    public static String getByCode(String code){
        for (HxResultErrorCodeEnum each: HxResultErrorCodeEnum.values()) {
            if (code.equals(each.getCode())) {
                return each.getMessage();
            }
        }
        return "";
    }


}
