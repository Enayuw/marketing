package com.br.marketing.strategy;

public enum InterfaceHandlerEnum {

    ARTIFICIAL_BLACK_LIST(1, "人工黑名单"),
    ARTIFICIAL_TRANSFER(2, "人工转化"),
    CUSTOMER_BLACK_LIST(4, "客服黑名单"),
    CUSTOMER_TRANSFER(3, "客服转化"),
    ARTIFICIAL_DIAL_PUSH(5, "人工拨打推送"),
    MESSAGE_DELAY(6, "消息延迟"),
    UNDEFINED(7, "未定义接口"),
    ARTIFICIAL_REAL_TIME_USERDATA(8, "人工实时推送用户名单"),
    ARTIFICIAL_BATCH_REALTIME_DATA(9, "人工推电销批量接口"),
    BATCH_MESSAGE_DELAY(10, "消息延迟批量接口"),
    ARTIFICIAL_SHUHE_BLACK_DATA(11, "数禾黑名单推人工转化接口"),
    MULTIPLE_DASSBATCH_CUSTOMERBLACK(12, "推送电销和客服黑名单组合接口"),
    INIT_TO_POLICY(13, "原始数据推送决策接口"),
    ZHONGAN_LOCK_DATA_INSERT(14, "众安明细锁定数据落库"),
    XIE_CHENG_CALL_RECORD_INSERT_DB(15, "携程通话明细保存到携程推营销数据表(3710058/3710078)"),
    ARTIFICIAL_IBU_BATCH_DATA(16, "人工IBU批量接口"),
    CUSTOMER_TRANSFER_SOLE(17, "客服转化去重"),
    INIT_TO_POLICY_SOLE(18, "推送决策去重接口"),
    ARTIFICIAL_REAL_TIME_LOG(19, "人工实时推送并且记录日志"),
    DIDI_CALL_RECORD_INSERT_DB(20, "滴滴通话明细保存到滴滴销数据表"),
    CUSTOMER_BLACKLIST_DISTRIBUTE(21, "推送客服黑名单(分发到多个apicode)"),
    CUSTOMER_TRANSFER_BY_CONVTYPE(22, "根据转化规则，推送客服转化"),
    ARTIFICIAL_REAL_TIME_USERDATA_SOLE(23, "人工实时推送用户名单-有去重能力"),
    ARTIFICIAL_TRANSFER_SOLE(24, "人工转化-有去重能力"),
    ARTIFICIAL_REAL_TIME_USERDATA_AND_CUSTOMER_TRANSFER_SOLE(25, "(人工实时推送用户名单-有去重能力)+客服转化去重"),
    XIE_CHENG_CALL_RECORD_INSERT_DB_VT(26, "携程通话明细保存到携程推营销数据表(3710090/3710091)"),
    CUSTOMER_TRANSFER_SOLE_QIFU(27, "客服转化去重（奇富360专用）"),
    CUSTOMER_TRANSFER_SOLE_STATUS(28, "客服转化去重+场景"),
    YIXIN_REALTIME_TO_POLICY(29, "宜信实时数据推送决策接口(定制)"),
    XIE_CHENG_CPA_FILTER_INSERT_DB(30, "携程CPA撞库过滤数据落库(定制)"),

    WUBA_CALL_RECORD_ADD_DB(31, "58新客通话明细入库-3710155"),
    CUSTOMER_TRANSFER_SOLE_USE_STATUS(32, "客服转化去重+状态自定义"),

    BIOCLOO_BLACK_LIST(33, "百可录黑名单"),
    CUSTOMER_AUTO_FILTRATION_RS(34, "榕树自动化过滤不去重(分发到多个apiCode)"),
    CUSTOMER_AUTO_FILTRATION_SOLE_RS(35, "榕树自动化过滤去重(分发到多个apiCode)"),
    CALLRECORD_MESSAGE_DELAY(36, "通话明细消息延迟批量接口"),
    ZHONGAN_SMS_LOCK_DATA_INSERT(37, "众安短信明细锁定数据落库"),
    DIDI_SMS_CALLBACK(38, "滴滴短信明细锁定数据落库"),
    DIDI_CALL_RECORD(39, "滴滴通话明细锁定数据落库"),
    ;


    InterfaceHandlerEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    private final Integer code;
    private final String name;

    public Integer getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static InterfaceHandlerEnum getHandlerEnum(int code) {
        for (InterfaceHandlerEnum handlerEnum : InterfaceHandlerEnum.values()) {
            if (handlerEnum.getCode().equals(code)) {
                return handlerEnum;
            }
        }
        return InterfaceHandlerEnum.UNDEFINED;
    }
}
