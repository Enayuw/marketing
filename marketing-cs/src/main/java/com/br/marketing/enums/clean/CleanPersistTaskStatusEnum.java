package com.br.marketing.enums.clean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件清洗持久化任务状态枚举
 * @see com.br.marketing.entity.MarketingCleanPersistTask#status
 */
@Getter
@AllArgsConstructor
public enum CleanPersistTaskStatusEnum {

    PENDING(0, "待执行"),
    RUNNING(1, "执行中"),
    SUCCESS(2, "成功"),
    FAIL(3, "失败");

    private final Integer code;
    private final String desc;

    public static CleanPersistTaskStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CleanPersistTaskStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
