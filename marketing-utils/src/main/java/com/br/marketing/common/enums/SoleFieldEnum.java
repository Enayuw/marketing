package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 去重维度 枚举类
 *
 * @author zhen.Li
 * @dateTime 2023/03/22 17:30
 */
@Getter
@AllArgsConstructor
public enum SoleFieldEnum {

    CUST_NUM_SOLE(1, "案件编号去重"),
    CELL_SOLE(2, "手机号去重"),
    CELL_STATUS_SOLE(3, "手机号+状态去重"),
    CUST_NUM_STATUS_SOLE(4, "案件编号+状态去重"),
    ;

    private Integer value;
    private String desc;

    /**
     * 获取枚举集合
     */
    public static List<Integer> getValues() {
        return Arrays.stream(SoleFieldEnum.values()).map(soleFieldEnum -> soleFieldEnum.getValue()).collect(Collectors.toList());
    }


}
