package com.br.marketing.enums.clean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否持久化到业务表（b_sync_config.is_pers）
 * @see com.br.marketing.entity.SyncConfig#isPers
 */
@Getter
@AllArgsConstructor
public enum IsPersEnum {

    NO(0, "否"),
    YES(1, "是");

    private final Integer code;
    private final String desc;

    public static boolean isPers(Integer code) {
        return YES.getCode().equals(code);
    }

    public static IsPersEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (IsPersEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
