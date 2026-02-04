package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：redis值类型枚举
 *
 * @author junzhe.ma
 * @date 2026-01-28 15:23
 */
@Getter
@AllArgsConstructor
public enum RedisValueTypeEnum {

    String("String"),
    Set("Set"),
    Hash("Hash");

    private final String value;

}
