package com.br.marketing.enums;

import java.util.Objects;

/**
 * 同程 sync_record 选码卡片：{@code b_marketing_tcyr_sync_record.assign_status}。
 * <ul>
 *   <li>库中 {@code NULL}：未发起卡片，对应枚举 {@link #NOT_STARTED}</li>
 *   <li>{@code 1}：已发起 tcapiCodeAssign</li>
 *   <li>{@code 2}：已回填 apiCode</li>
 * </ul>
 */
public enum TcyrAssignStatusEnum {

    /** 未发起选码卡片（库字段为 NULL） */
    NOT_STARTED(null, "未发起卡片"),
    /** 已发起 tcapiCodeAssign（Monkey CAS 成功后写入） */
    CARD_DISPATCHED(1, "已发起tcapiCodeAssign"),
    /** 已回填 apiCode（inner tcapiCodeFill 成功后写入） */
    FILLED(2, "已回填apiCode");

    private final Integer value;
    private final String desc;

    TcyrAssignStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 落库值；{@link #NOT_STARTED} 对应列为 NULL。
     */
    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 由库中 assign_status 解析；{@code null} 解析为 {@link #NOT_STARTED}。
     */
    public static TcyrAssignStatusEnum fromDbValue(Integer assignStatus) {
        if (assignStatus == null) {
            return NOT_STARTED;
        }
        for (TcyrAssignStatusEnum e : values()) {
            if (e.value != null && e.value.equals(assignStatus)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isNotStarted(Integer assignStatus) {
        return assignStatus == null;
    }

    public static boolean isCardDispatched(Integer assignStatus) {
        return Objects.equals(CARD_DISPATCHED.value, assignStatus);
    }

    public static boolean isFilled(Integer assignStatus) {
        return Objects.equals(FILLED.value, assignStatus);
    }
}
