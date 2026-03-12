package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * b_file_sync_task 推送目标类型
 */
@Getter
@AllArgsConstructor
public enum PushTargetTypeEnum {
    FROM_CONFIG(0, "从配置中获取"),
    SPECIFIED_TARGET(1, "指定目标路径");

    private final Integer value;
    private final String desc;

    public static PushTargetTypeEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (PushTargetTypeEnum e : values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }
}
