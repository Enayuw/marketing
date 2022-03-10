package com.br.marketing.strategy;

public enum InterfaceHandlerEnum {

    ARTIFICIAL_BLACK_LIST(1, "人工黑名单"),
    ARTIFICIAL_TRANSFER(2, "人工转化"),
    CUSTOMER_BLACK_LIST(3, "客服黑名单"),
    CUSTOMER_TRANSFER(4, "客服转化"),
    ARTIFICIAL_DIAL_PUSH(5, "人工拨打推送"),
    UNDEFINED(6, "未定义接口");

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
