package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName ExecuteTimeEnum
 * @Author kongbx
 * @Date 2025/9/10 14:52
 */
@Getter
@AllArgsConstructor
public enum ExecuteTimeEnum {

    YESTERDAY("0","T-1日")
    ,TODAY("1","T日");

    private String value;
    private String desc;

}
