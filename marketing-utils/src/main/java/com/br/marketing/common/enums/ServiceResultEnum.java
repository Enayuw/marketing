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

    // 0x 访问成功
    SUCCESS("000000", "成功"),
    SUCCESS_1("01", "参数错误,请检查参数"),

    SUCCESS_2("02", "内容校验失败,请修改内容"),
    SUCCESS_3("03", "规则重复,请重新定义规则"),
    SUCCESS_4("100004", "此配置已存在!"),
    AUTH_FAILED_ERROR_HEADER("200001", "权限名不能以*开头"),
    AUTH_FAILED_ERROR_PARAM("200003", "参数错误"),
    AUTH_CHECK_CODE_ERROR("200004", "验证码错误"),
    AUTH_LOGIN_PASS_ERROR("200005", "用户名或者密码错误"),

    AUTH_PASSWD_ERROR("200006", "密码错误"),

    AUTH_USER_INVALID_SESSION_ERROR("200006", "session过期"),

    AUTH_USER_REPEAT("200007", "帐号已存在！！"),

    DELETE_ROLE_ERROR("200008", "当前角色存在用户使用，禁止删除！"),
    EDIT_PASSWORD("200009", "当前用户为首次登录，需要修改密码"),
    AUTH_USER_API_CODE_ERROR("200010", "当前用户没有该apiCode权限"),
    AUTH_LOGIN_NO_PERMISSION("200011", "无自动化测试登陆权限"),
    //访问成功业务自定义通用状态
    SUCCESS_5("05", "服务开小差了"),
    SUCCESS_6("06", "规则名称重复"),

    // 5xxx 访问失败
    UNKNOWN_ERROR("5000", "遇到未知错误，请稍后重试"),
    FAILED("5001", "服务器正忙，请稍后再试"),
    //访问失败自定义通用状态
    FAILED_5("5005", "服务走丢了"),

    HALOBUTTONDISABLE("300001", "哈啰按钮禁止使用！"),
    INVALID_CID("300003", "无效的cide"),
    HALO_NO_DATA("300002", "没有可以清洗的数据！")
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
