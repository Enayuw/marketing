package com.br.marketing.enums.clean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据清洗状态枚举
 */

@Getter
@AllArgsConstructor
public enum DataCleanStatusEnum {

    READY(0, "待清洗"),
    RUNNING(1, "清洗中"),
    COMPLETE(2, "清洗完成");

    private Integer code;
    private String desc;


}
