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
     * 360有效期变更重推
     */
    QI_FU(1),
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
        throw new IllegalArgumentException("No enum value found for code: " + code);
    }
}
