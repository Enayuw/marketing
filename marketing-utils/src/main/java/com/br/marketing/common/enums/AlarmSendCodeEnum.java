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
    EXCEPTION_VALIDITY_PERIOD("62005", "有效期规则提示"),
    //pulsar消费requestId冲突
    REQUESTID_CONFLICT("62006", "requestIdConflict"),
    // 接口字段新增检查,阶梯推送，一般
    EXCEPTION_NEW_FIELD_CHECK("62006", "接口字段新增检查"),
    // 一般通知,阶梯推送，一般
    EXCEPTION_USUAL_NOTICE("62007", "通知"),
    //众安通话明细回调
    EXCEPTION_ZHONGAN_CALL_RECORD("62008", "众安通话明细回调"),
    //携程业务报错
    XIECHENG_RECORD("62009", "携程业务报错"),
    //业务未知错误,立即推送，63000
    SERVICEERROR_UNKNOWN("63000", "业务未知错误"),
    //三方接口错误,立即推送，64000
    INTERFACE_ERROR("64000", "三方接口错误"),

    //数据治理平台调用marketing-inner-api邮件发送接口使用
    DATA_GOVERNANCE_PLATFORM_SEND_EMAIL("70000", "数据治理平台邮件发送"),
    //宜信非实时推客服告警,立即推送
    EXCEPTION_YIXIN_PUSH_CUSTOMER("62010", "宜信非实时推客服"),
    EXCEPTION_WUBA("62058", "58业务报错code"),

    EXCEPTION_QIFU_ALARM("62360", "360业务告警码"),
    EXCEPTION_WBXK_ALARM("6001001", "58业务告警码");

    /**
     * 2022/10/28 17:30 发送码
     */
    private final String code;

    /**
     * 2022/10/28 17:30 消息
     */
    private final String message;

}
