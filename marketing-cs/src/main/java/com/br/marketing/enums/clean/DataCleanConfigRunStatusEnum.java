package com.br.marketing.enums.clean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 清洗通用配置-配置运行状态
 */

@Getter
@AllArgsConstructor
public enum DataCleanConfigRunStatusEnum {

    READY(0, "未运行"),
    RUNNING(1, "运行中");

    private  Integer code;
    private  String desc;

}
