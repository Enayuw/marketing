package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 营销中台前端交互
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 10:30
 */
@Getter
@AllArgsConstructor
public enum ServiceResultEnum {

    // 2xxx 访问成功
    SUCCESS("2000", "成功"),
    SUCCESS_1("2001", "参数错误,请检查参数"),
    SUCCESS_2("2002", "内容校验失败,请修改内容"),
    SUCCESS_3("2003", "规则重复,请重新定义规则"),
    //访问成功业务自定义通用状态
    SUCCESS_5("2005", "服务开小差了"),

    // 5xxx 访问失败
    UNKNOWN_ERROR("5000", "遇到未知错误，请稍后重试"),
    FAILED("5001", "服务器正忙，请稍后再试"),
    //访问失败自定义通用状态
    FAILED_5("5005", "服务走丢了"),
    ;

    /**
     * 2021/9/1 10:33 状态码
     */
    private final String code;

    /**
     * 2021/9/1 10:33 消息
     */
    private final String message;

}
