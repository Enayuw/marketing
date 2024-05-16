package com.br.marketing.enums;

/**
 * @ClassName FunctionNameEnum
 * @Description 注解函数枚举
 * @Author kongbx
 * @Date 2024/4/22 14:45
 */
public enum FunctionNameEnum {

    GET_USER_NAME("getUserName"),
            ;
    private final String name;

    FunctionNameEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
