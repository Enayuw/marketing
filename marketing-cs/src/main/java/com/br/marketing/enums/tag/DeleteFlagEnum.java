package com.br.marketing.enums.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 删除标记枚举
 * @author guangxiu.li
 * @date 2025/03/18
 */
@Getter
@AllArgsConstructor
public enum DeleteFlagEnum {

    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举值
     */
    public static DeleteFlagEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeleteFlagEnum value : DeleteFlagEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断是否已删除
     */
    public static boolean isDeleted(Integer code) {
        return DELETED.getCode().equals(code);
    }

    /**
     * 判断是否未删除
     */
    public static boolean isNotDeleted(Integer code) {
        return NOT_DELETED.getCode().equals(code);
    }
}