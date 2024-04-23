package com.br.marketing.enums;

import lombok.Getter;

/**
 * @ClassName InterfaceOperationsEnum
 * @Description 接口操作枚举
 * @Author kongbx
 * @Date 2024/4/22 18:04
 */
@Getter
public enum InterfaceOperationsEnum {

    XIECHENG_INSERT_DATA("700001","携程生成数据接口"),
    ;
    /**
     * 接口状态码
     */
    private final String code;

    /**
     * 信息
     */
    private final String message;

    InterfaceOperationsEnum(String code, String message) {
        this.code = code;
        this.message=message;
    }

}
