package com.br.marketing.api.customer.black.handler;

import lombok.Getter;

/**
 * 客户黑名单处理枚举
 *
 * @author senyang.zheng
 * @date 2024/10/30
 */
@Getter
public enum CustomerBlackHandlerEnum {

    /** 外星人黑名单 */
    B_ALIEN_DEFAULT("外星人黑名单", Boolean.FALSE),

    /** 国美黑名单 */
    B_GUME("国美黑名单", Boolean.FALSE,  "7492805"),
    ;


    private String name;

    private Boolean isNeedDecrypt;

    private String[] apiCodes;


    CustomerBlackHandlerEnum(String name, Boolean isNeedDecrypt, String... apiCodes) {
        this.name = name;
        this.apiCodes = apiCodes;
        this.isNeedDecrypt = isNeedDecrypt;
    }

    /**
     * 根据apiCode匹配定制黑名单策略
     *
     * @param apiCode       API代码
     * @param defaultCustom 默认自定义
     * @return {@link CustomerBlackHandlerEnum }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    public static CustomerBlackHandlerEnum valueOf(String apiCode, CustomerBlackHandlerEnum defaultCustom) {
        for (CustomerBlackHandlerEnum e : values()) {
            for (String code : e.apiCodes) {
                if (code.equals(apiCode)) {
                    return e;
                }
            }
        }
        return defaultCustom;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setApiCodes(String[] apiCodes) {
        this.apiCodes = apiCodes;
    }
}
