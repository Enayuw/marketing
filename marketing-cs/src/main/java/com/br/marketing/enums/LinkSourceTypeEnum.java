package com.br.marketing.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 链路来源类型枚举
 *
 * @author bingxu.kong
 * @since 2025/01/30
 */
@Getter
@AllArgsConstructor
public enum LinkSourceTypeEnum {

    MANUAL("MANUAL", "手动创建"),
    AUTO("AUTO", "自动发现");

    /**
     * 类型编码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String desc;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举值，不存在则返回null
     */
    public static LinkSourceTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (LinkSourceTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断编码是否有效
     *
     * @param code 编码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}
