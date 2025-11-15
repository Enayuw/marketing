package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomerEnum {

    TEST(1, "测试用户"),

    INNER_TEST(2, "内测用户"),

    PROD(3, "生产用户"),
    ;

    private final Integer code;

    private final String message;
}
