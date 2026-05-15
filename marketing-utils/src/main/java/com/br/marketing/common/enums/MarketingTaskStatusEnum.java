package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * b_marketing_task.status 取值说明。
 */
@Getter
@AllArgsConstructor
public enum MarketingTaskStatusEnum {

    /** 删除 */
    DELETED(0, "删除"),
    /** 正常，可参与跑分调度 */
    ENABLED(1, "正常"),
    /** 禁用 */
    DISABLED(2, "禁用");

    private final Integer value;
    private final String desc;
}
