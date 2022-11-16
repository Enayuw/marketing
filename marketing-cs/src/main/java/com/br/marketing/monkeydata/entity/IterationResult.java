package com.br.marketing.monkeydata.entity;

import lombok.Data;

import java.util.List;

@Data
public class IterationResult<I,R extends InputDataCondition> {

    /**
     * 输入数据列表
     */
    List<I> inputDataList;

    /**
     * 获取数据源条件
     */
    R inDatacondition;

    /**
     * 是否是单次处理
     *   如果该字段为true，获取数据源的循环只获取一次
     *   如果该字段为false，退出循环需要依靠外层result的code值为FAIL
     */
    Boolean isSingle;
}
