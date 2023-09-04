package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 营销中台报警发送码 枚举类
 *
 * @author juanjuan.song@brgroup.com
 * @dateTime 2022/10/28 17:30
 */
@Getter
@AllArgsConstructor
public enum AlarmSendCodeEnum {

    //主动发送——成功通知(开发,测试,产品,运营),立即推送
    SUCCESS_UPLOAD("50000", "uploadSuccess"),
    //主动发送——成功通知(开发,测试),立即推送
    SUCCESS_INTERNAL("51000", "InternalSuccessNotice"),
    //未知错误,立即推送
    ERROR_UNKNOWN("60000", "sysError"),
    //主动发送——业务异常,立即推送
    EXCEPTION_URGENT("61000", "dataExceptionUrgent"),
    //主动发送——业务异常,阶梯推送
    EXCEPTION_COMMON("62000", "dataExceptionCommonly"),
    //萨摩耶转化数据报警，手机号缺失,阶梯推送
    EXCEPTION_SAMOYE("62001", "samoyeCommonly"),
    //画像返回98,阶梯推送
    EXCEPTION_HUAX("62002", "huaxiangCommonly"),
    //滴滴联合建模,阶梯推送
    EXCEPTION_DIDI("62003", "didiCommonly"),
    //滴滴联合建模,阶梯推送
    EXCEPTION_SPEEDCOMMONCONFIG("62004", "marketingCommonConfigAlarm"),
    // 有效期配置异常,阶梯推送，一般
    EXCEPTION_VALIDITY_PERIOD("62005", "未配置有效期规则"),
    ;

    /**
     * 2022/10/28 17:30 发送码
     */
    private final String code;

    /**
     * 2022/10/28 17:30 消息
     */
    private final String message;

}
