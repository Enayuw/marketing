package com.br.marketing.enums;

import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

/**
 * 剔除规则数据来源枚举
 */
@Getter
public enum TcCpaDeleteRuleSourceTypeEnum {

    LOCK_DATA(1, "b_tcyr_cpa_lock_data lockData", "lockData.user_key", "lockData.lock_belong",
            " lockData.release_time > curdate() and lockData.is_del = 1"),
    BLANK_DATA(2, "b_tcyr_cpa_blank_data blank", "blank.user_key", null, " blank.is_del = 1"),
    INVALUE_DATA(3, "b_tcyr_cpa_invalue_data invalue", "invalue.user_key", "invalue.fail_msg", " invalue.is_del = 1"),

    CUSTOMIZE(9, null,  null,null, null),
    ;

    private static final Map<Integer, TcCpaDeleteRuleSourceTypeEnum> ENUM_MAP = new HashMap<>();

    static {
        for (TcCpaDeleteRuleSourceTypeEnum value : TcCpaDeleteRuleSourceTypeEnum.values()) {
            ENUM_MAP.put(value.value, value);
            ENUM_MAP.put(value.value, value);
        }
    }

    public static TcCpaDeleteRuleSourceTypeEnum getByValue(Integer value) {
        return ENUM_MAP.get(value);
    }

    TcCpaDeleteRuleSourceTypeEnum(Integer value, String tableName, String select, String field, String defaultCondition) {
        this.value = value;
        this.tableName = tableName;
        this.select = select;
        this.field = field;
        this.defaultCondition = defaultCondition;
    }

    private final Integer value;

    private final String tableName;

    private final String select;

    private final String field;

    private final String defaultCondition;
}
