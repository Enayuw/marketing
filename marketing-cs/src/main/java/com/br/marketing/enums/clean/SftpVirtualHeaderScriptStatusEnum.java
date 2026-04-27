package com.br.marketing.enums.clean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SFTP 虚拟表头脚本配置状态
 *
 * @see com.br.marketing.entity.SftpVirtualHeaderScriptConfig#status
 */
@Getter
@AllArgsConstructor
public enum SftpVirtualHeaderScriptStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer value;
    private final String desc;

    public static SftpVirtualHeaderScriptStatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (SftpVirtualHeaderScriptStatusEnum e : values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }
}
