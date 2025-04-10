package com.br.marketing.enums.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标签状态枚举
 * @author guangxiu.li
 * @date 2025/03/18
 */
@Getter
@AllArgsConstructor
public enum TagStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举值
     */
    public static TagStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TagStatusEnum value : TagStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断是否启用
     */
    public static boolean isEnabled(Integer code) {
        return ENABLED.getCode().equals(code);
    }

    /**
     * 判断是否禁用
     */
    public static boolean isDisabled(Integer code) {
        return DISABLED.getCode().equals(code);
    }

    /**
     * 获取状态描述
     */
    public static String getDesc(Integer code) {
        TagStatusEnum statusEnum = getByCode(code);
        return statusEnum != null ? statusEnum.getDesc() : "";
    }
}