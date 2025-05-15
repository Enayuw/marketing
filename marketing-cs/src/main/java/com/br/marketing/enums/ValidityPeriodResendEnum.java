package com.br.marketing.enums;

import lombok.Getter;

/**
 * 有效期变更重推类型枚举
 *
 * @author senyang.zheng
 * @date 2023/10/08
 */
@Getter
public enum ValidityPeriodResendEnum {
    /**
     * 默认不推
     */
    DEFAULT(0),
    /**
     * 重推执行通用转化数据规则处理流程
     */
    UNIVERSAL_TRANSFER_PROCESS_RESEND(1),
    /**
     * 转化数据(T-N有效)执行通用规则重推流程
     */
    UNIVERSAL_TRANSFER_PROCESS_OFFSET_DAY_RESEND(2),

    CUSTOMIZE_TRANSFER_PROCESS_RESEND(3),
    /**
     * 重推通用转化数据至规则编排通用分发队列
     */
    MRP_UNIVERSAL_TRANSFER_PROCESS_RESEND(4)
    ;

    private final Integer code;


    ValidityPeriodResendEnum(Integer code) {
        this.code = code;
    }


    /**
     * 根据code获取枚举
     *
     * @param code 枚举值
     * @return {@link ValidityPeriodResendEnum }
     * @author senyang.zheng
     * @date 2023/10/08
     */
    public static ValidityPeriodResendEnum getEnumByCode(Integer code) {
        for (ValidityPeriodResendEnum enumValue : ValidityPeriodResendEnum.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        //根据Code未找到对应枚举返回默认值
        return ValidityPeriodResendEnum.DEFAULT;
    }
}
