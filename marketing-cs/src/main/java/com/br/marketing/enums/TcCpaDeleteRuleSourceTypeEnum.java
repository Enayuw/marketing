package com.br.marketing.enums;

import lombok.Getter;

/**
 * 推送文件状态枚举
 */
@Getter
public enum TcCpaDeleteRuleSourceTypeEnum {

    LOCK_DATA(1, "b_tcyr_cpa_lock_data", "lock_belong"),
    BLANK_DATA(2, "b_tcyr_cpa_blank_data", null),
    INVALUE_DATA(3, "b_tcyr_cpa_invalue_data", "fail_msg"),

    CUSTOMIZE(9, null, null),
    ;

    TcCpaDeleteRuleSourceTypeEnum(Integer value, String tableName, String field) {
        this.value = value;
        this.tableName = tableName;
        this.field = field;
    }

    private final Integer value;

    private final String tableName;

    private final String field;
}
