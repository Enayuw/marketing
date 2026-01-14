package com.br.marketing.enums.clean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 衍生类型枚举
 */
@Getter
@AllArgsConstructor
public enum DerivedTypeEnum {
    /**
     * 普通字段（非衍生）
     */
    NORMAL(0, "普通"),
    
    /**
     * 衍生字段
     */
    DERIVED(1, "衍生");

    /**
     * 类型编码
     */
    private final Integer code;

    /**
     * 类型描述
     */
    private final String desc;

    /**
     * 根据编码获取枚举
     */
    public static DerivedTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DerivedTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断编码是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
