package com.br.marketing.enums.clean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据源类型枚举
 */

@Getter
@AllArgsConstructor
public enum DataSourceTypeEnum {

    GENERAL_INTERFACE(0, "通用调用"),
    ORIGINAL_INTERFACE(1, "定制接口清洗后调用");

    private Integer code;
    private String desc;


}
