package com.br.marketing.enums.sync;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * b_sync_config.is_unzip 是否需要解压
 */
@Getter
@AllArgsConstructor
public enum SyncConfigIsUnzipEnum {

    NO(0, "否"),
    YES(1, "是");

    private final Integer code;
    private final String desc;

    public static SyncConfigIsUnzipEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SyncConfigIsUnzipEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    /** 与库表 NOT NULL DEFAULT 0 一致 */
    public static Integer defaultIfNull(Integer code) {
        return code != null ? code : NO.getCode();
    }

    public static boolean needUnzip(Integer code) {
        return YES.getCode().equals(code);
    }
}
